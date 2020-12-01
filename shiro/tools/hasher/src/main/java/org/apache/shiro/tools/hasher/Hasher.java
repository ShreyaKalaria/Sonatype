/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shiro.tools.hasher;

import org.apache.commons.cli.*;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.UnknownAlgorithmException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.JavaEnvironment;
import org.apache.shiro.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Commandline line utility to hash data such as strings, passwords, resources (files, urls, etc).
 * <p/>
 * Usage:
 * <pre>
 * java -jar shiro-tools-hasher<em>-version</em>-cli.jar
 * </pre>
 * This will print out all supported options with documentation.
 *
 * @since 1.2
 */
public final class Hasher {

    private static final Option ALGORITHM = new Option("a", "algorithm", true, "hash algorithm name.  Defaults to MD5.");
    private static final Option DEBUG = new Option("d", "debug", false, "show additional error (stack trace) information.");
    private static final Option HELP = new Option("help", "help", false, "show this help message.");
    private static final Option HEX = new Option("h", "hex", false, "display a hex value instead of Base64.");
    private static final Option ITERATIONS = new Option("i", "iterations", true, "number of hash iterations.  Defaults to 1.");
    private static final Option NO_FORMAT = new Option("nf", "noformat", false, "turn off output formatting.  Any generated salt will be placed after the hash separated by a space.");
    private static final Option PASSWORD = new Option("p", "password", false, "hash a password (disable typing echo)");
    private static final Option PASSWORD_NC = new Option("pnc", "pnoconfirm", false, "disable password hash confirmation prompt.");
    private static final Option RESOURCE = new Option("r", "resource", false, "read and hash the resource located at <value>.  See below for more information.");
    private static final Option SALT = new Option("s", "salt", true, "use the specified salt.  <arg> is plaintext.");
    private static final Option SALT_BYTES = new Option("sb", "saltbytes", true, "use the specified salt bytes.  <arg> is hex or base64 encoded text.");
    private static final Option SALT_GEN = new Option("gs", "gensalt", false, "generate and use a random salt.");
    private static final Option SALT_GEN_HEX = new Option("gsh", "gensalthex", false, "display the generated salt's hex value instead of Base64.");
    private static final Option SALT_GEN_SIZE = new Option("gss", "gensaltsize", true, "the number of salt bits (not bytes!) to generate.  Defaults to 128.");
    private static final Option SHIRO = new Option("shiro", "shiro", false, "display output in the Shiro password file format (.ini [users] config).");

    private static final String HEX_PREFIX = "0x";
    private static final String DEFAULT_ALGORITHM_NAME = "MD5";
    private static final int DEFAULT_GENERATED_SALT_SIZE = 128;
    private static final int DEFAULT_NUM_ITERATIONS = 1;
    private static final String SALT_MUTEX_MSG = createMutexMessage(SALT, SALT_BYTES);


    public static void main(String[] args) {

        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(HELP).addOption(DEBUG).addOption(ALGORITHM).addOption(HEX).addOption(ITERATIONS);
        options.addOption(RESOURCE).addOption(PASSWORD).addOption(PASSWORD_NC);
        options.addOption(SALT).addOption(SALT_BYTES).addOption(SALT_GEN).addOption(SALT_GEN_SIZE).addOption(SALT_GEN_HEX);
        options.addOption(NO_FORMAT).addOption(SHIRO);

        boolean debug = false;
        String algorithm = DEFAULT_ALGORITHM_NAME;
        int iterations = DEFAULT_NUM_ITERATIONS;
        boolean base64 = true;
        boolean resource = false;
        boolean password = false;
        boolean passwordConfirm = true;
        String saltString = null;
        String saltBytesString = null;
        boolean generateSalt = false;
        boolean generatedSaltBase64 = true;
        int generatedSaltSize = DEFAULT_GENERATED_SALT_SIZE;

        boolean shiroFormat = false;
        boolean format = true;

        char[] passwordChars = null;

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(HELP.getOpt())) {
                printHelpAndExit(options, null, debug, 0);
            }
            if (line.hasOption(DEBUG.getOpt())) {
                debug = true;
            }
            if (line.hasOption(ALGORITHM.getOpt())) {
                algorithm = line.getOptionValue(ALGORITHM.getOpt());
            }
            if (line.hasOption(ITERATIONS.getOpt())) {
                iterations = getRequiredPositiveInt(line, ITERATIONS);
            }
            if (line.hasOption(HEX.getOpt())) {
                base64 = false;
            }
            if (line.hasOption(PASSWORD.getOpt())) {
                password = true;
            }
            if (line.hasOption(RESOURCE.getOpt())) {
                resource = true;
            }
            if (line.hasOption(PASSWORD_NC.getOpt())) {
                passwordConfirm = false;
            }
            if (line.hasOption(SALT.getOpt())) {
                saltString = line.getOptionValue(SALT.getOpt());
            }
            if (line.hasOption(SALT_BYTES.getOpt())) {
                saltBytesString = line.getOptionValue(SALT_BYTES.getOpt());
            }
            if (line.hasOption(SALT_GEN.getOpt())) {
                generateSalt = true;
            }
            if (line.hasOption(SALT_GEN_HEX.getOpt())) {
                generateSalt = true;
                generatedSaltBase64 = false;
            }
            if (line.hasOption(SALT_GEN_SIZE.getOpt())) {
                generateSalt = true;
                generatedSaltSize = getRequiredPositiveInt(line, SALT_GEN_SIZE);
                if (generatedSaltSize % 8 != 0) {
                    throw new IllegalArgumentException("Generated salt size must be a multiple of 8 (e.g. 128, 192, 256, 512, etc).");
                }
            }
            if (line.hasOption(NO_FORMAT.getOpt())) {
                format = false;
            }
            if (line.hasOption(SHIRO.getOpt())) {
                shiroFormat = true;
            }

            String sourceValue = null;

            Object source;

            if (password) {
                passwordChars = readPassword(passwordConfirm);
                source = passwordChars;
            } else {
                String[] remainingArgs = line.getArgs();
                if (remainingArgs == null || remainingArgs.length != 1) {
                    printHelpAndExit(options, null, debug, -1);
                }

                assert remainingArgs != null;
                sourceValue = toString(remainingArgs);

                if (resource) {
                    if (!ResourceUtils.hasResourcePrefix(sourceValue)) {
                        source = toFile(sourceValue);
                    } else {
                        source = ResourceUtils.getInputStreamForPath(sourceValue);
                    }
                } else {
                    source = sourceValue;
                }
            }

            ByteSource salt = getSalt(saltString, saltBytesString, generateSalt, generatedSaltSize);

            SimpleHash hash = new SimpleHash(algorithm, source, salt, iterations);

            StringBuilder output;
            if (shiroFormat) {
                output = formatForShiroIni(hash, base64, salt, generatedSaltBase64, generateSalt);
            } else if (format) {
                output = format(hash, base64, salt, generatedSaltBase64, generateSalt, algorithm, sourceValue);
            } else {
                output = formatMinimal(hash, base64, salt, generatedSaltBase64, generateSalt);
            }

            System.out.println(output);

        } catch (IllegalArgumentException iae) {
            exit(iae, debug);
        } catch (UnknownAlgorithmException uae) {
            exit(uae, debug);
        } catch (IOException ioe) {
            exit(ioe, debug);
        } catch (Exception e) {
            printHelpAndExit(options, e, debug, -1);
        } finally {
            if (passwordChars != null && passwordChars.length > 0) {
                for (int i = 0; i < passwordChars.length; i++) {
                    passwordChars[i] = ' ';
                }
            }
        }
    }

    private static String createMutexMessage(Option... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("The ");

        for (int i = 0; i < options.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Option o = options[0];
            sb.append("-").append(o.getOpt()).append("/--").append(o.getLongOpt());
        }
        sb.append(" and generated salt options are mutually exclusive.  Only one of them may be used at a time");
        return sb.toString();
    }

    private static void exit(Exception e, boolean debug) {
        printException(e, debug);
        System.exit(-1);
    }

    private static StringBuilder format(ByteSource hash, boolean hashBase64, ByteSource salt, boolean saltBase64, boolean showSalt, String alg, String value) {
        StringBuilder sb = new StringBuilder();

        sb.append(alg).append("(").append(value).append(")");

        if (hashBase64) {
            sb.append(" base64 = ").append(hash.toBase64());
        } else {
            sb.append(" hex = ").append(hash.toHex());
        }

        if (showSalt && salt != null) {
            sb.append("\nGenerated salt");
            if (saltBase64) {
                sb.append(" base64 = ").append(salt.toBase64());
            } else {
                sb.append(" hex = ").append(salt.toHex());
            }
        }

        return sb;
    }

    private static StringBuilder formatForShiroIni(ByteSource hash, boolean hashBase64, ByteSource salt, boolean saltBase64, boolean showSalt) {
        StringBuilder sb = new StringBuilder();

        if (hashBase64) {
            sb.append(hash.toBase64());
        } else {
            //hex:
            sb.append(HEX_PREFIX).append(hash.toHex());
        }

        if (showSalt && salt != null) {
            sb.append(" ");
            if (saltBase64) {
                sb.append(salt.toBase64());
            } else {
                //hex:
                sb.append(HEX_PREFIX).append(salt.toHex());
            }
        }
        return sb;
    }

    private static StringBuilder formatMinimal(ByteSource hash, boolean hashBase64, ByteSource salt, boolean saltBase64, boolean showSalt) {
        StringBuilder sb = new StringBuilder();

        if (hashBase64) {
            sb.append(hash.toBase64());
        } else {
            sb.append(hash.toHex());
        }

        if (showSalt && salt != null) {
            sb.append(" ");
            if (saltBase64) {
                sb.append(salt.toBase64());
            } else {
                sb.append(salt.toHex());
            }
        }

        return sb;
    }

    private static int getRequiredPositiveInt(CommandLine line, Option option) {
        String iterVal = line.getOptionValue(option.getOpt());
        try {
            return Integer.parseInt(iterVal);
        } catch (NumberFormatException e) {
            String msg = "'" + option.getLongOpt() + "' value must be a positive integer.";
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static ByteSource getSalt(String saltString, String saltBytesString, boolean generateSalt, int generatedSaltSize) {

        if (saltString != null) {
            if (generateSalt || (saltBytesString != null)) {
                throw new IllegalArgumentException(SALT_MUTEX_MSG);
            }
            return ByteSource.Util.bytes(saltString);
        }

        if (saltBytesString != null) {
            if (generateSalt) {
                throw new IllegalArgumentException(SALT_MUTEX_MSG);
            }

            String value = saltBytesString;
            boolean base64 = true;
            if (saltBytesString.startsWith(HEX_PREFIX)) {
                //hex:
                base64 = false;
                value = value.substring(HEX_PREFIX.length());
            }
            byte[] bytes;
            if (base64) {
                bytes = Base64.decode(value);
            } else {
                bytes = Hex.decode(value);
            }
            return ByteSource.Util.bytes(bytes);
        }

        if (generateSalt) {
            SecureRandomNumberGenerator generator = new SecureRandomNumberGenerator();
            int byteSize = generatedSaltSize / 8; //generatedSaltSize is in *bits* - convert to byte size:
            return generator.nextBytes(byteSize);
        }

        //no salt used:
        return null;
    }

    private static void printException(Exception e, boolean debug) {
        if (e != null) {
            System.out.println();
            if (debug) {
                System.out.println("Error: ");
                e.printStackTrace(System.out);
                System.out.println(e.getMessage());

            } else {
                System.out.println("Error: " + e.getMessage());
                System.out.println();
                System.out.println("Specify -d or --debug for more information.");
            }
        }
    }

    private static void printHelp(Options options, Exception e, boolean debug) {
        HelpFormatter help = new HelpFormatter();
        String command = "java -jar shiro-tools-hasher-<version>.jar [options] [<value>]";
        String header = "\nPrint a cryptographic hash (aka message digest) of the specified <value>.\n--\nOptions:";
        String footer = "\n" +
                "<value> is optional only when hashing passwords (see below).  It is\n" +
                "required all other times." +
                "\n\n" +
                "Password Hashing:\n" +
                "---------------------------------\n" +
                "Specify the -p/--password option and DO NOT enter a <value>.  You will\n" +
                "be prompted for a password and characters will not echo as you type." +
                "\n\n" +
                "Salting:\n" +
                "---------------------------------\n" +
                "Specifying a salt:" +
                "\n\n" +

                "You may specify a salt using the -s/--salt option followed by the salt\n" +
                "value.  If the salt value is a base64 or hex string representing a\n" +
                "byte array, you must specify the -sb/--saltbytes option to indicate this,\n" +
                "otherwise the text value bytes will be used directly." +
                "\n\n" +
                "When using -sb/--saltbytes, the -s/--salt value is expected to be a\n" +
                "base64-encoded string by default.  If the value is a hex-encoded string,\n" +
                "you must prefix the string with 0x (zero x) to indicate a hex value." +
                "\n\n" +
                "Generating a salt:" +
                "\n\n" +
                "Use the -sg/--saltgenerated option if you don't want to specify a salt,\n" +
                "but want a strong random salt to be generated and used during hashing.\n" +
                "The generated salt size defaults to 128 bytes.  You may specify\n" +
                "a different size by using the -sgs/--saltgeneratedsize option followed by\n" +
                "a positive integer." +
                "\n\n" +
                "Because a salt must be specified if computing the\n" +
                "hash later, generated salts will be printed, defaulting to base64\n" +
                "encoding.  If you prefer to use hex encoding, additionally use the\n" +
                "-sgh/--saltgeneratedhex option." +
                "\n\n" +
                "Files, URLs and classpath resources:\n" +
                "---------------------------------\n" +
                "If using the -r/--resource option, the <value> represents a resource path.\n" +
                "By default this is expected to be a file path, but you may specify\n" +
                "classpath or URL resources by using the classpath: or url: prefix\n" +
                "respectively." +
                "\n\n" +
                "Some examples:" +
                "\n\n" +
                "<command> -r fileInCurrentDirectory.txt\n" +
                "<command> -r ../../relativePathFile.xml\n" +
                "<command> -r ~/documents/myfile.pdf\n" +
                "<command> -r /usr/local/logs/absolutePathFile.log\n" +
                "<command> -r url:http://foo.com/page.html\n" +
                "<command> -r classpath:/WEB-INF/lib/something.jar";

        printException(e, debug);

        System.out.println();
        help.printHelp(command, header, options, null);
        System.out.println(footer);
    }

    private static void printHelpAndExit(Options options, Exception e, boolean debug, int exitCode) {
        printHelp(options, e, debug);
        System.exit(exitCode);
    }

    private static char[] readPassword(boolean confirm) {
        if (!JavaEnvironment.isAtLeastVersion16()) {
            String msg = "Password hashing (prompt without echo) uses the java.io.Console to read passwords " +
                    "safely.  This is only available on Java 1.6 platforms and later.";
            throw new IllegalArgumentException(msg);
        }
        java.io.Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("java.io.Console is not available on the current JVM.  Cannot read passwords.");
        }
        char[] first = console.readPassword("%s", "Password to hash: ");
        if (first == null || first.length == 0) {
            throw new IllegalArgumentException("No password specified.");
        }
        if (confirm) {
            char[] second = console.readPassword("%s", "Password to hash (confirm): ");
            if (!Arrays.equals(first, second)) {
                String msg = "Password entries do not match.";
                throw new IllegalArgumentException(msg);
            }
        }
        return first;
    }

    private static File toFile(String path) {
        String resolved = path;
        if (path.startsWith("~/") || path.startsWith(("~\\"))) {
            resolved = path.replaceFirst("\\~", System.getProperty("user.home"));
        }
        return new File(resolved);
    }

    private static String toString(String[] strings) {
        int len = strings != null ? strings.length : 0;
        if (len == 0) {
            return null;
        }
        return StringUtils.toDelimitedString(strings, " ");
    }

    static {
        ALGORITHM.setArgName("name");
        SALT_GEN_SIZE.setArgName("numBits");
        ITERATIONS.setArgName("num");
        SALT.setArgName("sval");
        SALT_BYTES.setArgName("encTxt");
    }
}
