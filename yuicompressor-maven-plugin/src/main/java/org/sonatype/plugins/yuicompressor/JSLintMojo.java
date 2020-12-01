/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plugins.yuicompressor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.sonatype.plexus.build.incremental.BuildContext;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_RESOURCES;

/**
 * Execute JSLint on javascript sources.
 *
 * @since 1.0
 */
@Mojo(name="jslint", defaultPhase = PROCESS_RESOURCES)
public class JSLintMojo
    extends ProcessSourcesMojoSupport
{
  public static final String[] DEFAULT_INCLUDES = { "**/*.js" };

  @Parameter(defaultValue = "${project.basedir}/src/main/js")
  private File sourceDirectory;

  // TODO support non-boolean options

  /**
   * JSLint options, a map, where key is jslint option name and value either true or false.
   *
   * @see <a href="https://github.com/douglascrockford/JSLint/blob/master/jslint.js">jslint.js</a>
   */
  @Parameter
  private Map<String, String> jslintOptions;

  @Parameter(defaultValue = "true")
  private boolean fail;

  @Override
  protected void processSources(List<File> sources) throws MojoExecutionException {
    int errors = 0;
    for (File source : sources) {
      Context cx = Context.enter();
      try {
        getLog().debug("Processing: " + source);
        int result = processSource(source, cx);
        if (result != 0) {
          if (fail) {
            getLog().error(source + ": " + result + " errors");
          }
          else {
            getLog().warn(source + ": " + result + " errors");
          }
        }
        errors += result;
      }
      catch (IOException e) {
        throw new MojoExecutionException("Could not execute jslint on " + source, e);
      }
      finally {
        Context.exit();
      }
    }

    if (errors != 0) {
      if (fail) {
        getLog().error("Found " + errors + " errors");
        throw new MojoExecutionException("Found " + errors + " errors");
      }
      else {
        getLog().warn("Found " + errors + " errors");
      }
    }
  }

  /**
   * Process source file, returns number of detected errors.
   */
  protected int processSource(File source, Context cx) throws IOException, MojoExecutionException {
    if (!buildContext.hasDelta(source)) {
      // limitation of buildcontext api, no way to report errors from previous executions
      return 0;
    }

    buildContext.removeMessages(source);

    Scriptable scope = cx.initStandardObjects();

    Reader fr = new InputStreamReader(getClass().getResourceAsStream("/jslint.js"));
    cx.evaluateReader(scope, fr, "jslint.js", 0, null);

    Function jslint = (Function) scope.get("JSLINT", scope);

    Scriptable options = cx.newObject(scope);
    if (jslintOptions != null) {
      for (Map.Entry<String, String> option : jslintOptions.entrySet()) {
        options.put(option.getKey(), options, toBoolean(option.getValue()));
      }
    }

    Object[] jsargs = { loadSource(source), options };

    boolean passed = (Boolean) jslint.call(cx, scope, scope, jsargs);
    if (passed) {
      return 0;
    }

    NativeArray errors = (NativeArray) jslint.get("errors", jslint);

    for (int i = 0; i < errors.getLength(); i++) {
      Scriptable error = (Scriptable) errors.get(i, errors);
      if (error == null) {
        // apparent bug in jslint, when "too many errors" is reported, last array element is null
        continue;
      }
      int line = ((Number) ScriptableObject.getProperty(error, "line")).intValue();
      int column = ((Number) ScriptableObject.getProperty(error, "character")).intValue();
      String reason = (String) ScriptableObject.getProperty(error, "reason");

      int severity = fail ? BuildContext.SEVERITY_ERROR : BuildContext.SEVERITY_WARNING;
      buildContext.addMessage(source, line, column, reason, severity, null);
    }

    return (int)errors.getLength();
  }

  private boolean toBoolean(String value) {
    return Boolean.parseBoolean(value);
  }

  private String loadSource(File source) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(source));
    try {
      return IOUtil.toString(is);
    }
    finally {
      is.close();
    }
  }

  @Override
  protected String[] getDefaultIncludes() {
    return DEFAULT_INCLUDES;
  }

  @Override
  protected File getSourceDirectory() {
    return sourceDirectory;
  }
}
