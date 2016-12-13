package ru.spbau.mit.Tester.ResultWriter;

import org.apache.commons.io.FileUtils;
import ru.spbau.mit.CreationAndConfigs.IntervalWithStep;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;


/**
 * Writes out a csv for each test variant (overwrites if we already have such a csv file) +
 * writes a csv with fixed parameters of the test
 * <p>
 * We're given a dir to write out the results to, we assume it's empty at the start
 * If our latest dir has 7 * 3 * 2 files we create the next folder
 */
public class ResultWriter {
    private final File dir;

    public ResultWriter(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new NotADirectoryException();
        }

        File toDir = getHalfFullDir(dir);
        if (toDir == null) {
            this.dir = new File(dir,
                    Integer.toString(dir.listFiles().length));
            FileUtils.forceMkdir(this.dir);
        } else {
            this.dir = toDir;
        }
    }

    private static File getHalfFullDir(File dir) {
        File[] files = dir.listFiles();
        if (files.length == 0)
            return null;

        Arrays.sort(files);
        File f = files[files.length - 1];

        if (f.isDirectory() && f.listFiles().length <
                ServerType.validValues().size() * 3 * 2) {
            return f;
        }
        return null;
    }

    private File createFile(UserConfig config) throws IOException {
        String name = config.getServerType().toString() +
                ":" + config.getVaryingParameter().toString();
        File f = new File(dir, name);
        if (!f.exists()) {
            f.createNewFile();
        }
        return f;
    }

    private static final String headerFormat = "{0}, perSort, perRequest, perClient";
    private static final String format = "{0}, {1}, {2}, {3}";

    public void writeResults(UserConfig config, IntervalWithStep step, List<RunResults> results) throws IOException {
        File file = createFile(config);
        PrintWriter pw = new PrintWriter(file);

        String write = MessageFormat.format(headerFormat, config.getVaryingParameter());
        pw.println(write);
        for (int i = 0; i < results.size(); ++i) {
            RunResults res = results.get(i);
            write = MessageFormat.format(format,
                    Integer.toString(step.getStart() + i * step.getStep()),
                    Long.toString(res.perSort),
                    Long.toString(res.perRequest),
                    Long.toString(res.perClient));
            pw.println(write);
        }
        pw.close();
        writeHelperFile(config, step, file);
    }

    private static final String fixedArgs = "{0}, {1}, {2}, {3}";

    private void writeHelperFile(UserConfig configInit, IntervalWithStep step, File mainFile) throws IOException {
        UserConfig config = configInit.clone();
        File helperFile = new File(mainFile.getAbsolutePath() + ":init");
        if (!mainFile.exists()) {
            mainFile.createNewFile();
        }
        PrintWriter pw = new PrintWriter(helperFile);
        String write = MessageFormat.format(fixedArgs,
                VaryingParameter.ELEMENTS_PER_REQ,
                VaryingParameter.CLIENTS_PARALLEL,
                VaryingParameter.TIME_DELTA,
                "TOTAL_REQUESTS_PER_CLIENT");
        pw.println(write);

        config.setVarying(step.getStart());
        write = MessageFormat.format(fixedArgs,
                Integer.toString(config.getArraySize()),
                Integer.toString(config.getClientsSize()),
                Integer.toString(config.getNextReqDelta()),
                Integer.toString(config.getRequestsPerClient()));
        pw.println(write);
        pw.close();
    }

    private class NotADirectoryException extends IOException {
    }
}
