package com.kloudnuk.webserver.services;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kloudnuk.webserver.services.api.IDataStoreManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Map;

import java.io.File;
import java.io.IOException;

@Service("mongoManager")
public class MongoDbManager implements IDataStoreManager {

    private static final Logger log = LoggerFactory.getLogger(MongoDbManager.class);
    private ApplicationProps props = new ApplicationProps();
    private String wd = "/nuk/mongodb_scripts/";
    private String user = props.getPublicKey();
    private String pass = props.getPrivKey();
    private String projid = props.getProjectid();

    protected static class CmdBuilder {
        static ProcessBuilder pb = null;
        static Map<String, String> env = null;

        static void init(String... cmd) {
            pb = null;
            env = null;
            pb = new ProcessBuilder(cmd);
            env = pb.environment();
        }

        static void withVariable(String name, String value) {
            env.put(name, value);
        }

        static void withoutVariable(String name) {
            env.remove(name);
        }

        static void withWorkingDir(File directory) {
            pb.directory(directory);
        }

        static void withOutputRedirect(File output, boolean isLog) {
            if (isLog) {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));
            } else {
                pb.redirectOutput(ProcessBuilder.Redirect.to(output));
            }

        }

        static void withErrorRedirect(File errorLog) {
            pb.redirectError(ProcessBuilder.Redirect.appendTo(errorLog));
        }

        static CompletableFuture<Process> buildexec() throws IOException {
            Process p = pb.start();
            log.info("new cmd-builder process executed... " + p.pid());
            return p.onExit();
        }
    }

    @Override
    public void createDbUser(String org, String uuid) {
        try {
            String cmd = wd + "createdbuser.sh";
            CmdBuilder.init(cmd, user, pass, projid, org, uuid);
            CmdBuilder.withOutputRedirect(new File(wd, "users.log"), true);
            CmdBuilder.withErrorRedirect(new File(wd, "error.log"));
            CompletableFuture<Process> future = CmdBuilder.buildexec();
            future.join();
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
    }

    @Override
    public File createX509Cert(String uuid, int expMonths) throws NullPointerException {
        try {
            String cmd = wd + "/createdbusercert.sh";
            LocalDateTime ldt = LocalDateTime.now();
            String now = ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            File cert = File.createTempFile(uuid + "_" + now, ".pem");
            CmdBuilder.init(cmd, user, pass, projid, uuid, String.valueOf(expMonths));
            CmdBuilder.withOutputRedirect(cert, false);
            CmdBuilder.withErrorRedirect(new File(wd + "/error.log"));
            CompletableFuture<Process> future = CmdBuilder.buildexec();
            future.join();
            return cert;
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
            return null;
        }
    }

    @Override
    public void removeDbUser(String uuid) throws NullPointerException {
        try {
            String cmd = wd + "/removedbuser.sh";
            CmdBuilder.init(cmd, user, pass, projid, uuid);
            CmdBuilder.withOutputRedirect(new File(wd, "users.log"), true);
            CmdBuilder.withErrorRedirect(new File(wd + "/error.log"));
            CompletableFuture<Process> future = CmdBuilder.buildexec();
            future.join();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
