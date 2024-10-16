package org.maven.ide.eclipse.authentication.internal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.maven.ide.eclipse.authentication.AuthRegistryException;
import org.maven.ide.eclipse.authentication.AuthenticationType;
import org.maven.ide.eclipse.authentication.IAuthData;
import org.maven.ide.eclipse.authentication.IAuthRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthRealm
    implements IAuthRealm
{
    private final static Logger log = LoggerFactory.getLogger( AuthRealm.class );

    /**
     * The path to the root node for the auth registry. Each realm will be saved in a child of this node.
     */
    private static final String SECURE_NODE_PATH = "org.maven.ide.eclipse.authentication.registry";

    /**
     * The key to save the username into.
     */
    private static final String SECURE_USERNAME = "username";

    /**
     * The key to save the password into.
     */
    private static final String SECURE_PASSWORD = "password";

    /**
     * Local filesystem path to ssl client certificate
     */
    private static final String SECURE_SSL_CERTIFICATE_PATH = "sslCertificatePath";

    /**
     * Passphrase to access ssl certificate
     */
    private static final String SECURE_SSL_CERTIFICATE_PASSPHRASE = "sslCertificatePassphrase";

    private ISecurePreferences secureStorage;

    private String id;

    private String username = "";

    private String password = "";

    private File sslCertificatePath;

    private String sslCertificatePassphrase;

    private String name;

    private AuthenticationType authenticationType = AuthenticationType.USERNAME_PASSWORD;

    public AuthenticationType getAuthenticationType()
    {
        return authenticationType;
    }

    public void setAuthenticationType( AuthenticationType authenticationType )
    {
        this.authenticationType = authenticationType;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    private String description;

    AuthRealm( ISecurePreferences secureStorage, String id, String username, String password, File sslCertificate,
               String sslCertificatePassphrase )
    {
        this.secureStorage = secureStorage;
        this.id = id;
        this.username = username;
        this.password = password;
        this.sslCertificatePath = sslCertificate;
        this.sslCertificatePassphrase = sslCertificatePassphrase;
    }

    public AuthRealm( String id, String name, String description, AuthenticationType authenticationType ) {
        this.secureStorage = null;
        this.id = id;
        this.name = name;
        this.description = description;
        this.authenticationType = authenticationType;
    }

    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public boolean isAnonymous()
    {
        if ( username != null && username.trim().length() != 0 )
        {
            return false;
        }

        if ( sslCertificatePath != null )
        {
            return false;
        }

        return true;
    }

    public File getCertificatePath()
    {
        return sslCertificatePath;
    }

    public String getCertificatePassphrase()
    {
        return sslCertificatePassphrase;
    }

    private static <T> boolean eq( T a, T b )
    {
        return a != null ? a.equals( b ) : b == null;
    }

    private String encodeRealmId( String realmId )
        throws StorageException
    {
        // realmId = normalizeRealmId( realmId );
        try
        {
            return EncodingUtils.encodeSlashes( EncodingUtils.encodeBase64( realmId.getBytes( "UTF-8" ) ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new StorageException( StorageException.INTERNAL_ERROR, e );
        }
    }

    private boolean saveToSecureStorage()
    {
        if ( secureStorage == null )
        {
            log.debug( "Secure storage not available, can't save security realm authentication data." );
            return false;
        }

        ISecurePreferences authNode = secureStorage.node( SECURE_NODE_PATH );

        try
        {
            String nodeName = encodeRealmId( id );
            authNode.put( id, nodeName, false );

            ISecurePreferences realmNode = authNode.node( nodeName );

            realmNode.put( SECURE_USERNAME, username, true );
            realmNode.put( SECURE_PASSWORD, password, true );

            String sslCertificatePathString = sslCertificatePath != null ? sslCertificatePath.getCanonicalPath() : null;
            realmNode.put( SECURE_SSL_CERTIFICATE_PATH, sslCertificatePathString, false );
            realmNode.put( SECURE_SSL_CERTIFICATE_PASSPHRASE, sslCertificatePassphrase, true );

            authNode.flush();

            return true;
        }
        catch ( StorageException e )
        {
            log.error( "Error saving auth data for realm id '" + id + "': " + e.getMessage(), e );
            throw new AuthRegistryException( e );
        }
        catch ( IOException e )
        {
            log.error( "Error saving auth data for realm id '" + id + "': " + e.getMessage(), e );
            throw new AuthRegistryException( e );
        }
    }

    void loadFromSecureStorage( ISecurePreferences secureStorage )
    {
        if ( secureStorage == null )
        {
            log.debug( "Secure storage not available, can't load security realm authentication data." );
            return;
        }
        try
        {
            log.debug( "Loading authentication realm {} from secure storage", id );

            this.secureStorage = secureStorage;

            ISecurePreferences authNode = secureStorage.node( SECURE_NODE_PATH );
            ISecurePreferences realmNode = authNode.node( encodeRealmId( id ) );

            username = realmNode.get( SECURE_USERNAME, "" );
            password = realmNode.get( SECURE_PASSWORD, "" );

            String sslCertificatePathString = realmNode.get( SECURE_SSL_CERTIFICATE_PATH, null );
            sslCertificatePath = sslCertificatePathString != null ? new File( sslCertificatePathString ) : null;
            sslCertificatePassphrase = realmNode.get( SECURE_SSL_CERTIFICATE_PASSPHRASE, null );
        }
        catch ( StorageException e )
        {
            log.error( "Error loading authentication realm from node " + id, e );
        }
    }

    void removeFromSecureStorage( ISecurePreferences secureStorage )
    {
        if ( secureStorage == null )
        {
            return;
        }
        try
        {
            log.debug( "Removing authentication realm {} from secure storage", id );

            this.secureStorage = secureStorage;

            ISecurePreferences authNode = secureStorage.node( SECURE_NODE_PATH );
            String realmId = encodeRealmId( id );
            if ( !authNode.nodeExists( realmId ) )
            {
                return;
            }
            ISecurePreferences realmNode = authNode.node( realmId );
            realmNode.removeNode();
            authNode.flush();
        }
        catch ( Exception e )
        {
            log.error( "Error removing authentication realm node for realm " + id, e );
        }
    }

    @Override
    public String toString()
    {
        return getId();
    }

    public boolean setAuthData( IAuthData authData )
    {
        if ( authData.getAuthenticationType() != null && !authData.getAuthenticationType().equals( authenticationType ) )
        {
            throw new AuthRegistryException( "Security realm " + id + ": The authentication type of the realm "
                + authenticationType + " does not match the authentication type of the provided authentication data "
                + authData.getAuthenticationType() );
        }

        boolean needsSave = false;
        if ( authData.allowsUsernameAndPassword() )
        {
            String newUsername = authData.getUsername();
            if ( newUsername == null )
            {
                newUsername = "";
            }
            String newPassword = authData.getPassword();
            if ( newPassword == null )
            {
                newPassword = "";
            }
            if ( !eq( username, newUsername ) || !eq( password, newPassword ) )
            {
                username = newUsername;
                password = newPassword;
                log.debug( "Setting authentication for realm id '{}': User name: '{}'", id, username );
                needsSave = true;
            }
        }
        if ( authData.allowsCertificate() )
        {
            File newSslCertificatePath = authData.getCertificatePath();
            if ( newSslCertificatePath != null )
            {
                try
                {
                    newSslCertificatePath = newSslCertificatePath.getCanonicalFile();
                }
                catch ( IOException e )
                {
                    throw new AuthRegistryException( e );
                }
            }

            if ( !eq( sslCertificatePath, newSslCertificatePath )
                || !eq( sslCertificatePassphrase, authData.getCertificatePassphrase() ) )
            {
                sslCertificatePath = newSslCertificatePath;
                sslCertificatePassphrase = authData.getCertificatePassphrase();
                log.debug( "Setting authentication for realm id '{}': Certificate file name: '{}'", id,
                           sslCertificatePath );
                needsSave = true;
            }
        }

        if ( needsSave )
        {
            return saveToSecureStorage();
        }

        return false;
    }

    public IAuthData getAuthData()
    {
        IAuthData authData = new AuthData( authenticationType );
        if ( authData.allowsUsernameAndPassword() )
        {
            authData.setUsernameAndPassword( username, password );
        }
        if ( authData.allowsCertificate() )
        {
            authData.setSSLCertificate( sslCertificatePath, sslCertificatePassphrase );
        }
        return authData;
    }
}
