package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Vault Invoker Service which handles actually calling vault to do import/export operations and dealing with the
 * class loaders to do so.
 */
public class VaultInvokerServiceImpl implements VaultInvokerService {

    public static final String LIB = "lib";
    public static final String BIN = "bin";
    private static final String VAULT3_CLASS = "org.apache.jackrabbit.vault.cli.VaultFsApp";
    private static final String VAULT_METHOD = "main";
    private static final Logger log = Logger.getInstance(VaultInvokerServiceImpl.class);
    private ClassLoader vaultClassLoader;
    private boolean init = false;

    @Override
    public void invokeVault(String vaultDir, String[] args) throws IntelliVaultException {
        try {
            initVault(vaultDir);

            log.info("executing vlt with params: " + Arrays.toString(args));

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(vaultClassLoader);
                Class<?> vltClass = Class.forName(VAULT3_CLASS, true, vaultClassLoader);
                Method vltMethod = vltClass.getMethod(VAULT_METHOD, String[].class);
                vltMethod.invoke(null, new Object[] { args });
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }

        } catch (Exception e) {
            throw new IntelliVaultException(e);
        }
    }

    @Override
    public void forceReInit() {
        init = false;
    }

    /**
     * Initialize vault.  Basically finds all the jars in the vault folder and creates a custom class loader which
     * includes those jars.  All vault operations are then executed using that class loader.
     *
     * @param vaultDir the vault home directory as specified in the settings dialog. Could be the root directory, or
     *                 potentially the bin or lib directory.
     * @throws IOException if an error occurs, such as the vault directory not being set.
     */
    private void initVault(String vaultDir) throws IOException {
        if (!init) {
            if (vaultDir == null || vaultDir.trim().length() == 0) {
                throw new IOException("Vault Directory not set");
            }

            if (vaultDir.endsWith(BIN)) {
                vaultDir = vaultDir.substring(0, vaultDir.lastIndexOf(File.separator));
            }

            if (!vaultDir.endsWith(LIB)) {
                vaultDir += File.separator + LIB;
            }

            List<URL> libList = new ArrayList<URL>();
            File libDir = new File(vaultDir.replace('/', File.separatorChar));
            File[] libs = libDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jar")) || (name.endsWith(".zip"));
                }
            });
            if (libs != null) {
                for (File lib : libs) {
                    try {
                        libList.add(lib.toURI().toURL());
                        String libName = lib.getName();
                    } catch (IOException e) {
                        log.error("error loading lib " + lib.getAbsolutePath(), e);
                    }
                }

                vaultClassLoader = new URLClassLoader(libList.toArray(new URL[libList.size()]));
                init = true;
            }

        }
    }
}
