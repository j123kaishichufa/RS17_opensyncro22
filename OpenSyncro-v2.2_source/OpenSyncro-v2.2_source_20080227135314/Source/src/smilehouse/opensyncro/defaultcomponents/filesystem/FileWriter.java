package smilehouse.opensyncro.defaultcomponents.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import smilehouse.opensyncro.pipes.component.FailTransferException;
import smilehouse.opensyncro.pipes.component.PipeComponentUtils;
import smilehouse.opensyncro.pipes.log.MessageLogger;

public abstract class FileWriter {
    
    
    protected void write(File to, String writeThis, boolean append, MessageLogger logger, String charset)
        throws FailTransferException {
        OutputStreamWriter out = null;

        logger.logMessage("Writing " + writeThis.length() + " characters to file \""
            + to.getPath() + "\"", this, MessageLogger.DEBUG);

        try {
            out = new OutputStreamWriter(new FileOutputStream(to, append), charset);
            out.write(writeThis);
        } catch(UnsupportedEncodingException e) {
            logger.logMessage(
                "Unsupported encoding: " + charset + ", aborting",
                this,
                MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } catch(IOException e) {
            logger.logMessage("IOException while writing to destination file \"" + to.getName()
                    + "\", aborting", this, MessageLogger.ERROR);
            PipeComponentUtils.failTransfer();
        } finally {
            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch(IOException e) {
                    logger.logMessage("IOException while closing destination file \""
                            + to.getName() + "\", aborting", this, MessageLogger.ERROR);
                    PipeComponentUtils.failTransfer();
                }
            }
        }
    }

}
