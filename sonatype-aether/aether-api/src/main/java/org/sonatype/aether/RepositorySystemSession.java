package org.sonatype.aether;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.util.Map;

import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencyManager;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.collection.DependencyTraverser;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.transfer.TransferListener;

/**
 * Defines settings and components that control the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface RepositorySystemSession
{

    /**
     * Indicates whether the repository system operates in offline mode and avoids/refuses any access to remote
     * repositories.
     * 
     * @return {@code true} if the repository system is in offline mode, {@code false} otherwise.
     */
    boolean isOffline();

    /**
     * Indicates whether transfer errors (e.g. unreachable host, bad authentication) from resolution attempts should be
     * cached in the local repository. If caching is enabled, resolution will not be reattempted until the update policy
     * for the affected resource has expired.
     * 
     * @return {@code true} if transfer errors are cached, {@code false} to always reattempt downloading.
     */
    boolean isTransferErrorCachingEnabled();

    /**
     * Indicates whether missing artifacts/metadata from resolution attempts should be cached in the local repository.
     * If caching is enabled, resolution will not be reattempted until the update policy for the affected resource has
     * expired.
     * 
     * @return {@code true} if missing resources are cached, {@code false} to always reattempt downloading.
     */
    boolean isNotFoundCachingEnabled();

    /**
     * Indicates whether missing artifact descriptors are silently ignored. If enabled and no artifact descriptor is
     * available, an empty stub descriptor is used instead.
     * 
     * @return {@code true} if missing artifact descriptors are ignored, {@code false} to fail the operation with an
     *         exception.
     */
    boolean isIgnoreMissingArtifactDescriptor();

    /**
     * Indicates whether invalid artifact descriptors are silently ignored. If enabled and an artifact descriptor is
     * invalid, an empty stub descriptor is used instead.
     * 
     * @return {@code true} if invalid artifact descriptors are ignored, {@code false} to fail the operation with an
     *         exception.
     */
    boolean isIgnoreInvalidArtifactDescriptor();

    /**
     * Gets the global checksum policy. If set, the global checksum policy overrides the checksum policies of the remote
     * repositories being used for resolution.
     * 
     * @return The global checksum policy or {@code null}/empty if not set and the per-repository policies apply.
     * @see RepositoryPolicy#CHECKSUM_POLICY_FAIL
     * @see RepositoryPolicy#CHECKSUM_POLICY_IGNORE
     * @see RepositoryPolicy#CHECKSUM_POLICY_WARN
     */
    String getChecksumPolicy();

    /**
     * Gets the global update policy. If set, the global update policy overrides the update policies of the remote
     * repositories being used for resolution.
     * 
     * @return The global update policy or {@code null}/empty if not set and the per-repository policies apply.
     * @see RepositoryPolicy#UPDATE_POLICY_ALWAYS
     * @see RepositoryPolicy#UPDATE_POLICY_DAILY
     * @see RepositoryPolicy#UPDATE_POLICY_NEVER
     */
    String getUpdatePolicy();

    /**
     * Gets the local repository used during this session. This is a convenience method for
     * {@link LocalRepositoryManager#getRepository()}.
     * 
     * @return The local repository being during this session, never {@code null}.
     */
    LocalRepository getLocalRepository();

    /**
     * Gets the local repository manager used during this session.
     * 
     * @return The local repository manager used during this session, never {@code null}.
     */
    LocalRepositoryManager getLocalRepositoryManager();

    /**
     * Gets the workspace reader used during this session. If set, the workspace reader will usually be consulted first
     * to resolve artifacts.
     * 
     * @return The workspace reader for this session or {@code null} if none.
     */
    WorkspaceReader getWorkspaceReader();

    /**
     * Gets the listener being notified of actions in the repository system.
     * 
     * @return The repository listener or {@code null} if none.
     */
    RepositoryListener getRepositoryListener();

    /**
     * Gets the listener being notified of uploads/downloads by the repository system.
     * 
     * @return The transfer listener or {@code null} if none.
     */
    TransferListener getTransferListener();

    /**
     * Gets the system properties to use, e.g. for processing of artifact descriptors. System properties are usually
     * collected from the runtime environment like {@link System#getProperties()} and environment variables.
     * 
     * @return The (read-only) system properties, never {@code null}.
     */
    Map<String, String> getSystemProperties();

    /**
     * Gets the user properties to use, e.g. for processing of artifact descriptors. User properties are similar to
     * system properties but are set on the discretion of the user and hence are considered of higher priority than
     * system properties.
     * 
     * @return The (read-only) user properties, never {@code null}.
     */
    Map<String, String> getUserProperties();

    /**
     * Gets the configuration properties used to tweak internal aspects of the repository system (e.g. thread pooling,
     * connector-specific behavior, etc.)
     * 
     * @return The (read-only) configuration properties, never {@code null}.
     * @see ConfigurationProperties
     */
    Map<String, Object> getConfigProperties();

    /**
     * Gets the mirror selector to use for repositories discovered in artifact descriptors. Note that this selector is
     * not used for remote repositories which are passed as request parameters to the repository system, those
     * repositories are supposed to denote the effective repositories.
     * 
     * @return The mirror selector to use, never {@code null}.
     */
    MirrorSelector getMirrorSelector();

    /**
     * Gets the proxy selector to use for repositories discovered in artifact descriptors. Note that this selector is
     * not used for remote repositories which are passed as request parameters to the repository system, those
     * repositories are supposed to have their proxy (if any) already set.
     * 
     * @return The proxy selector to use, never {@code null}.
     * @see org.sonatype.aether.repository.RemoteRepository#getProxy()
     */
    ProxySelector getProxySelector();

    /**
     * Gets the authentication selector to use for repositories discovered in artifact descriptors. Note that this
     * selector is not used for remote repositories which are passed as request parameters to the repository system,
     * those repositories are supposed to have their authentication (if any) already set.
     * 
     * @return The authentication selector to use, never {@code null}.
     * @see org.sonatype.aether.repository.RemoteRepository#getAuthentication()
     */
    AuthenticationSelector getAuthenticationSelector();

    /**
     * Gets the registry of artifact types recognized by this session.
     * 
     * @return The artifact type registry, never {@code null}.
     */
    ArtifactTypeRegistry getArtifactTypeRegistry();

    /**
     * Gets the dependency traverser to use for building dependency graphs.
     * 
     * @return The dependency traverser to use for building dependency graphs, never {@code null}.
     */
    DependencyTraverser getDependencyTraverser();

    /**
     * Gets the dependency manager to use for building dependency graphs.
     * 
     * @return The dependency manager to use for building dependency graphs, never {@code null}.
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the dependency selector to use for building dependency graphs.
     * 
     * @return The dependency selector to use for building dependency graphs, never {@code null}.
     */
    DependencySelector getDependencySelector();

    /**
     * Gets the dependency graph transformer to use for building dependency graphs.
     * 
     * @return The dependency graph transformer to use for building dependency graphs, never {@code null}.
     */
    DependencyGraphTransformer getDependencyGraphTransformer();

    /**
     * Gets the custom data associated with this session.
     * 
     * @return The session data, never {@code null}.
     */
    SessionData getData();

    /**
     * Gets the cache the repository system may use to save data for future reuse during the session.
     * 
     * @return The repository cache or {@code null} if none.
     */
    RepositoryCache getCache();

}
