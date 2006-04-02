package org.apache.maven.plugin.pmd;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * TODO: Description.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractPmdViolationCheckMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Fail on violation?
     *
     * @parameter expression="${failOnViolation}" default-value="true"
     * @required
     */
    private boolean failOnViolation;

    protected void executeCheck( String filename, String tagName )
        throws MojoFailureException, MojoExecutionException
    {
        File outputFile = new File( targetDirectory, filename );
        if ( outputFile.exists() )
        {
            try
            {
                XmlPullParser xpp = new MXParser();
                FileReader freader = new FileReader( outputFile );
                BufferedReader breader = new BufferedReader( freader );
                xpp.setInput( breader );

                int violations = countViolations( xpp, tagName );
                if ( violations > 0 && failOnViolation )
                {
                    throw new MojoFailureException(
                        "You have " + violations + " violation" + ( violations > 1 ? "s" : "" ) + "." );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to read PMD results xml: " + outputFile.getAbsolutePath(),
                                                  e );
            }
            catch ( XmlPullParserException e )
            {
                throw new MojoExecutionException( "Unable to read PMD results xml: " + outputFile.getAbsolutePath(),
                                                  e );
            }
        }
        else
        {
            throw new MojoFailureException( "Unable to perform check, " + "unable to find " + outputFile );
        }
    }

    private int countViolations( XmlPullParser xpp, String tagName )
        throws XmlPullParserException, IOException
    {
        int count = 0;

        int eventType = xpp.getEventType();
        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG && tagName.equals( xpp.getName() ) )
            {
                count++;
            }
            eventType = xpp.next();
        }

        return count;
    }
}
