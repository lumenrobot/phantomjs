/**
 * Created by Ari on 8/15/2016.
 */

import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.*;
import org.junit.Test;
import sun.plugin2.main.server.ResultHandler;

public class phantomPDF {
    private final File testDir = new File("src/test/scripts");

    private final File acroRd32Script = TestUtil.resolveScriptForOS(testDir + "/acrord32");


    public void testTutorialExample() throws Exception {

    final  long printJobTimeout = 15000;
        final boolean printInBackground = false;
        final  File pdfFile = new File("D:/lapor.pdf");

        ResultHandler resultHandler;
        try {
            System.out.print("[main] Preparing print job ...");
            resultHandler = print(pdfFile, printJobTimeout, printInBackground);
            System.out.println("[main] Successfully sent the print job ...");
        }catch (final Exception e){
            e.printStackTrace();
            fail("[main] Printing of the following document failed : "+pdfFile.getAbsolutePath());
            throw e;
        }

        System.out.println("[main] Test is existing but waiting for the print job to finish...");
        resultHandler.waitFor();
        System.out.println("[main] The print job has finished...");
    }

    public ResultHandler print(final File file, final long printJobTimeout, final boolean printInBackground)
    throws IOException{
        int exitValue;
        ExecuteWatchdog watchdog = null;
        ResultHandler resultHandler;

        final Map<String, File> map = new HashMap<String, File>();
        map.put("file",file);
        final  CommandLine commandLine = new CommandLine(acroRd32Script);
        commandLine.addArgument("/p");
        commandLine.addArgument("/h");
        commandLine.addArgument("${file}");
        commandLine.setSubstitutionMap(map);

        final Executor executor = new DefaultExecutor();
        executor.setExitValue(1);

        if(printJobTimeout > 0){
            watchdog = new ExecuteWatchdog(printJobTimeout);
            executor.setWatchdog(watchdog);
        }

        if(printInBackground){
            System.out.println("[print] Executing non-blocking print jobb ...");
            resultHandler = new ResultHandler(watchdog);
        }else{
            System.out.println("[print] Executing blocking print job ...");
            exitValue = executor.execute(commandLine);
            resultHandler = new ResultHandler(exitValue);
        }
        return resultHandler;
    }

    private class ResultHandler extends DefaultExecuteResultHandler{
        private ExecuteWatchdog watchdog;

        public ResultHandler(final ExecuteWatchdog watchdog){
            this.watchdog = watchdog;
        }

        public ResultHandler(final int exitValue){
            super.onProcessComplete(exitValue);
        }

        @Override
        public void onProcessComplete(int exitValue) {
            super.onProcessComplete(exitValue);
            System.out.println("[resultHandler] The document was successfully printed ...");
        }

        @Override
        public void onProcessFailed(ExecuteException e) {
            super.onProcessFailed(e);
            if(watchdog != null && watchdog.killedProcess()){
                System.err.println("[resultHandler] The print process timed out");
            }else{
                System.err.println("[resultHandler] The print process failed to do "+e.getMessage());
            }
        }
    }


}
