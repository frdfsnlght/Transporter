/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Patcher.java
 *
 * Created on Jun 13, 2011, 1:28:13 PM
 */
package com.frdfsnlght.transporter.client;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Patcher extends javax.swing.JFrame {

    private static String name;
    private static String version;

    /** Creates new form Patcher */
    public Patcher() {

        /*
        try {
            String textOut = "";
            for (int l = 1; l < 30000; l++) {
                if ((l % 1000) == 0)
                    System.out.println(l);
                textOut += "A";
                byte[] plainOut = textOut.getBytes("UTF-8");
                Cipher cipher = new Cipher(256);
                cipher.initEncrypt("tab".getBytes("UTF-8"));
                byte[] cipherOut = cipher.doFinal(plainOut);

                cipher = new Cipher(256);
                cipher.initDecrypt("tab".getBytes("UTF-8"));
                byte[] plainIn = cipher.doFinal(cipherOut);
                String textIn = new String(plainIn, "UTF-8");

                if (textOut.hashCode() != textIn.hashCode()) {
                    System.out.println("out.length=" + textOut.length());
                    System.out.println("out.hash=" + textOut.hashCode());
                    System.out.println("in.length=" + textIn.length());
                    System.out.println("in.hash=" + textIn.hashCode());
                    System.exit(1);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        */

        /*
        for (int in = 0; in < 1000000; in++) {
            byte[] data = new byte[4];
            data[1] = (byte)(0x00ff & (in >> 16));
            data[2] = (byte)(0x00ff & (in >> 8));
            data[3] = (byte)(0x00ff & in);

            int out =
                    (0x00ff0000 & ((int)data[1] << 16)) +
                    (0x0000ff00 & ((int)data[2] << 8)) +
                    (0x000000ff & (int)data[3]);

            if (in != out) {
                System.out.println("MISMATCH @ in=" + in);
            }
        }
        System.exit(1);
        */

        /*
        for (int i = 1; i < 20000; i++) {
            byte[] data = new byte[3];

            data[0] = (byte)(0x00ff & (i >> 16));
            data[1] = (byte)(0x00ff & (i >> 8));
            data[2] = (byte)(0x00ff & i);

            int x = (0x00ff0000 & ((int)data[0] << 16)) +
                    (0x0000ff00 & ((int)data[1] << 8)) +
                    (0x000000ff & (int)data[2]);

            if (i != x) {
                System.out.println("i=" + i);
                System.out.println("x=" + x);
                System.out.println(
                        Integer.toHexString(data[0]) + " " +
                        Integer.toHexString(data[1]) + " " +
                        Integer.toHexString(data[2])
                        );
                System.exit(1);
            }
        }

        System.exit(1);
        */

        /*
        try {
            for (int x = 1; x < 1000; x++) {
                System.out.println("------------- " + x);

                Message messageOut = new Message();
                for (int i = 0; i < x; i++)
//                    messageOut.put("key" + i, "value" + i);
                    messageOut.put("key" + i, null);

                String encodedOut = messageOut.encode();
                System.out.println("encodedOut.length=" + encodedOut.length());

                byte[] plainOut = encodedOut.getBytes("UTF-8");
                System.out.println("plainOut.length=" + plainOut.length);

                Cipher cipher = new Cipher(256);
                cipher.initEncrypt("tab".getBytes("UTF-8"));
                byte[] cipherOut = cipher.doFinal(plainOut);
                System.out.println("cipherOut.length=" + cipherOut.length);

                byte[] streamOut = new byte[cipherOut.length + 4];
                System.arraycopy(cipherOut, 0, streamOut, 4, cipherOut.length);
                streamOut[1] = (byte)(0x000000ff & (cipherOut.length >> 16));
                streamOut[2] = (byte)(0x000000ff & (cipherOut.length >> 8));
                streamOut[3] = (byte)(0x000000ff & cipherOut.length);
                System.out.println("streamOut.length=" + streamOut.length);

                // decode

                int recLen =
                        (0x00ff0000 & ((int)streamOut[1] << 16)) +
                        (0x0000ff00 & ((int)streamOut[2] << 8)) +
                        (0x000000ff & (int)streamOut[3]);
                System.out.println("recLen=" + recLen);

                if (recLen != cipherOut.length)
                    System.out.println("ERROR!!!");
                else
                    System.out.println("length is OK");

                byte[] cipherIn = Arrays.copyOfRange(streamOut, 4, recLen + 4);
                System.out.println("cipherIn.length=" + cipherIn.length);

                cipher = new Cipher(256);
                cipher.initDecrypt("tab".getBytes("UTF-8"));
                byte[] plainIn = cipher.doFinal(cipherIn);
                System.out.println("plainIn.length=" + plainIn.length);

                String encodedIn = new String(plainIn, "UTF-8");
                System.out.println("encodedIn.length=" + encodedIn.length());

                Message messageIn = Message.decode(encodedIn);
//                System.out.println(messageIn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        */

        /*
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\messages.txt"))));
            String line;
            for (;;) {
                line = r.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.length() == 0) continue;
                if (line.startsWith("#")) continue;

                byte[] plainDataIn = line.getBytes("UTF-8");

                System.out.println("plainDataIn.length=" + plainDataIn.length);

                Cipher cipher = new Cipher(256);
                cipher.initEncrypt("tab".getBytes("UTF-8"));
                byte[] cipherDataIn = cipher.doFinal(plainDataIn);
                System.out.println("cipherDataIn.length=" + cipherDataIn.length);

                cipher = new Cipher(256);
                cipher.initDecrypt("tab".getBytes("UTF-8"));
                byte[] plainDataOut = cipher.doFinal(cipherDataIn);
                System.out.println("plainDataOut.length=" + plainDataOut.length);

                String encoded = new String(plainDataOut, "UTF-8");

                Message m = Message.decode(encoded);
                System.out.println(m);
            }
            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        initComponents();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel clientPathLabel = new javax.swing.JLabel();
        clientPath = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        javax.swing.JLabel clientVersionLabel = new javax.swing.JLabel();
        clientVersion = new javax.swing.JTextField();
        javax.swing.JLabel clientStateLabel = new javax.swing.JLabel();
        clientState = new javax.swing.JTextField();
        javax.swing.JLabel pluginVersionLabel = new javax.swing.JLabel();
        javax.swing.JTextField pluginVersion = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        patchButton = new javax.swing.JButton();
        javax.swing.JScrollPane outputScroller = new javax.swing.JScrollPane();
        output = new javax.swing.JTextArea();
        javax.swing.JLabel outputLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(name + " Patcher");
        setIconImage(Toolkit.getDefaultToolkit().getImage(Patcher.class.getResource("/resources/client/bandaid.png")));
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);

        clientPathLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientPathLabel.setText("Client Path:");
        clientPathLabel.setFocusable(false);

        clientPath.setEditable(false);

        browseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/client/magnifier.png"))); // NOI18N
        browseButton.setToolTipText("Browse for the minecraft.jar file");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        clientVersionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientVersionLabel.setText("Client Version:");
        clientVersionLabel.setFocusable(false);

        clientVersion.setEditable(false);

        clientStateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clientStateLabel.setText("Client State:");
        clientStateLabel.setFocusable(false);

        clientState.setEditable(false);

        pluginVersionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pluginVersionLabel.setText(name + " Version:");
        pluginVersionLabel.setFocusable(false);

        pluginVersion.setEditable(false);
        pluginVersion.setText(version);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        patchButton.setText("Patch");
        patchButton.setEnabled(false);
        patchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchButtonActionPerformed(evt);
            }
        });

        output.setColumns(20);
        output.setEditable(false);
        output.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        output.setRows(5);
        outputScroller.setViewportView(output);

        outputLabel.setText("Output:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outputScroller, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(clientPathLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(clientVersionLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clientStateLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pluginVersionLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(clientPath, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                                .addGap(10, 10, 10)
                                .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(clientVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                            .addComponent(clientState, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                            .addComponent(pluginVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(patchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(closeButton))
                    .addComponent(outputLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientPathLabel)
                    .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clientPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientVersionLabel)
                    .addComponent(clientVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientStateLabel)
                    .addComponent(clientState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pluginVersionLabel)
                    .addComponent(pluginVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(patchButton))
                .addGap(4, 4, 4)
                .addComponent(outputLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputScroller, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR Files", "jar");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File(clientPath.getText()));
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        refreshClientInfo(file.getPath());
    }//GEN-LAST:event_browseButtonActionPerformed

    private void patchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patchButtonActionPerformed
        if (needForce) {
            int retVal = JOptionPane.showConfirmDialog(this, "Are you sure you want to apply the patch? Your client may become unusable if you continue.", "Force Patch", JOptionPane.YES_NO_OPTION);
            if (retVal == JOptionPane.NO_OPTION) return;
            append("Beginning forced patching process...");
        } else
        append("Beginning patching process...");

        final File jarFile = new File(clientPath.getText());
        File checkFile = new File(jarFile.getParentFile(), "minecraft-unpatched.jar");
        if (checkFile.exists()) {
            append("Backup file '" + checkFile.getPath() + "' exists.");
            int retVal = JOptionPane.showConfirmDialog(this, "A backup file of the client already exists. Should I overwrite it?", "Overwrite Backup", JOptionPane.YES_NO_CANCEL_OPTION);
            if (retVal == JOptionPane.CANCEL_OPTION) {
                append("Patch process aborted.");
                return;
            }
            if (retVal == JOptionPane.NO_OPTION) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(checkFile);
                chooser.setDialogTitle("Backup");
                retVal = chooser.showSaveDialog(this);
                if (retVal != JFileChooser.APPROVE_OPTION) {
                    append("Patch process aborted.");
                    return;
                }
                checkFile = chooser.getSelectedFile();
            }
        }
        final File backupFile = checkFile;

        patchButton.setEnabled(false);
        closeButton.setEnabled(false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // copy the jar file
                    append("Creating backup copy of the JAR file...");
                    FileChannel src = new FileInputStream(jarFile).getChannel();
                    FileChannel dst = new FileOutputStream(backupFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    File tmpFile = File.createTempFile(jarFile.getName(), null, jarFile.getParentFile());
                    tmpFile.deleteOnExit();

                    append("Copying and patching entries from JAR file...");
                    int copyCount = 0;
                    int patchCount = 0;
                    JarFile srcJar = new JarFile(jarFile);
                    JarOutputStream tmpJar = new JarOutputStream(new FileOutputStream(tmpFile));
                    for (Enumeration<JarEntry> e = srcJar.entries(); e.hasMoreElements(); ) {
                        JarEntry srcEntry = e.nextElement();
                        String path = srcEntry.getName();
                        if (path.indexOf("META-INF") == 0) continue;
                        if (newPatchFiles.containsKey(path)) continue;

                        InputStream is;
                        if (replacePatchFiles.containsKey(path)) {
                            is = getClass().getResourceAsStream("/resources/client/" + clientPatchKey + "/" + replacePatchFiles.get(path).path);
                            patchCount++;
                        } else {
                            is = srcJar.getInputStream(srcEntry);
                            copyCount++;
                        }

                        // copy the entry
                        JarEntry dstEntry = new JarEntry(path);
                        tmpJar.putNextEntry(dstEntry);
                        byte[] buf = new byte[1024];
                        while (true) {
                            int read = is.read(buf, 0, 1024);
                            if (read == -1) break;
                            tmpJar.write(buf , 0 , read);
                        }
                        tmpJar.closeEntry();
                    }

                    // add the new entries
                    append("Copying new entries to JAR file...");
                    int addCount = 0;
                    for (String path : newPatchFiles.keySet()) {
                        InputStream is = getClass().getResourceAsStream("/resources/client/" + clientPatchKey + "/" + newPatchFiles.get(path).path);

                        // copy the entry
                        JarEntry dstEntry = new JarEntry(path);
                        tmpJar.putNextEntry(dstEntry);
                        byte[] buf = new byte[1024];
                        while (true) {
                            int read = is.read(buf, 0, 1024);
                            if (read == -1) break;
                            tmpJar.write(buf , 0 , read);
                        }
                        tmpJar.closeEntry();
                        addCount++;
                    }

                    tmpJar.close();
                    append("Copied " + copyCount + " JAR entries.");
                    append("Patched " + patchCount + " JAR entries.");
                    append("Added " + addCount + " JAR entries.");

                    // copy the jar file
                    append("Copying patched JAR file to '" + jarFile.getPath() + "'...");
                    src = new FileInputStream(tmpFile).getChannel();
                    dst = new FileOutputStream(jarFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    // delete the old one
                    tmpFile.delete();
                    append("Deleted temporary file.");
                    append("Patch process complete.");

                } catch (IOException e) {
                    append(e);
                    append("What the hell just happened? Patch process aborted!");
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshClientInfo(jarFile.getPath());
                    }
                });
            }
        });
        t.start();
    }//GEN-LAST:event_patchButtonActionPerformed

    private void append(Throwable t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(os));
        try {
            append(os.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {}
    }

    private void append(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                output.append(msg + "\n");
            }
        });
    }

    private void refreshClientInfo(String clientPath) {
        clientVersion.setText("unknown");
        clientState.setText("unknown");
        if (output.getText().length() > 0)
            append("-------------------------------------");
        patchButton.setEnabled(false);
        closeButton.setEnabled(true);

        if (clientPath == null) {
            this.clientPath.setText("not found");
            return;
        }
        this.clientPath.setText(clientPath);
        File jarFile = new File(clientPath);
        if ((! jarFile.exists()) || (! jarFile.isFile()) || (! jarFile.canRead())) return;

        // get the client version
        clientPatchKey = null;
        File versionFile = new File(jarFile.getParentFile(), "version");
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(versionFile)));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    clientPatchKey = line;
                    break;
                }
            }
            r.close();
        } catch (IOException e) {
            append(e);
        }
        if (clientPatchKey == null) {
            clientVersion.setText("unable to read version");
            return;
        }
        append("Read version file '" + versionFile.getPath() + "'.");

        // find a matching patch set
        clientPatchProps = new Properties();
        InputStreamReader r = new InputStreamReader(this.getClass().getResourceAsStream("/resources/client/" + clientPatchKey + "/manifest"));
        try {
            clientPatchProps.load(r);
        } catch (IOException e) {
            append("No patch set found for version '" + clientPatchKey + "'");
            clientVersion.setText("unknown (probably new?)");
            return;
        }
        append("Found matching patch set for version '" + clientPatchKey + "'.");

        clientVersion.setText(clientPatchProps.getProperty("version") + " (patchable)");
        newPatchFiles = getNewFiles(clientPatchProps);
        replacePatchFiles = getReplaceFiles(clientPatchProps);
        patchButton.setEnabled(true);

        int newFilesFound = 0;
        int oldFilesFound = 0;
        int replacedFilesFound = 0;
        try {
            JarFile jar = new JarFile(jarFile);
            for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                JarEntry entry = e.nextElement();
                String path = entry.getName();
                if (newPatchFiles.containsKey(path)) {
                    PatchFile patchFile = newPatchFiles.get(path);
                    if (entry.getCrc() == patchFile.newCrc) {
                        append("Found patched '" + path + "'.");
                        newFilesFound++;
                    } else
                        append("Found unknown '" + path + "'.");
                } else if (replacePatchFiles.containsKey(path)) {
                    PatchFile patchFile = replacePatchFiles.get(path);
                    if (entry.getCrc() == patchFile.newCrc) {
                        append("Found patched '" + path + "'.");
                        replacedFilesFound++;
                    } else if (entry.getCrc() == patchFile.oldCrc) {
                        append("Found unpatched '" + path + "'.");
                        oldFilesFound++;
                    }
                }
            }
        } catch (IOException e) {
            append(e);
            clientState.setText("unknown (unable to read JAR file)");
            return;
        }

        if ((newFilesFound == 0) &&
            (replacedFilesFound == 0) &&
            (oldFilesFound == replacePatchFiles.size())) {
            clientState.setText("ready to patch");
            patchButton.setEnabled(true);
            return;
        }
        needForce = true;
        append("Patching will required forcing.");
        if ((newFilesFound == newPatchFiles.size()) &&
            (replacedFilesFound == replacePatchFiles.size()) &&
            (oldFilesFound == 0)) {
            clientState.setText("already patched");
            return;
        }
        clientState.setText("conflicting patch already installed");
    }

    private Map<String,PatchFile> getReplaceFiles(Properties props) {
        Map<String,PatchFile> files = new HashMap<String,PatchFile>();
        for (int i = 1; ; i++) {
            String line = props.getProperty("replace." + i);
            if (line == null) break;
            String parts[] = line.split(",");
            String path = parts[0];
            try {
                long newCrc = Long.decode(parts[1]);
                long oldCrc = Long.decode(parts[2]);
                files.put(path, new PatchFile(path, newCrc, oldCrc));
            } catch (NumberFormatException e) {}
        }
        return files;
    }

    private Map<String,PatchFile> getNewFiles(Properties props) {
        Map<String,PatchFile> files = new HashMap<String,PatchFile>();
        for (int i = 1; ; i++) {
            String line = props.getProperty("new." + i);
            if (line == null) break;
            String parts[] = line.split(",");
            String path = parts[0];
            try {
                long newCrc = Long.decode(parts[1]);
                files.put(path, new PatchFile(path, newCrc, 0));
            } catch (NumberFormatException e) {}
        }
        return files;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Properties plugin = loadProperties("/plugin.yml");
        name = plugin.getProperty("name");
        version = plugin.getProperty("version");
        version = version.replace("\"", "");

        File file = new File(System.getProperty("user.home") + File.separator + ".minecraft");
        if (! file.isDirectory()) {
            String appData = System.getenv("APPDATA");
            if (appData == null)
                appData = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming";
            file = new File(appData + File.separator + ".minecraft");
            if (! file.isDirectory())
                file = null;
        }

        final String clientPath;
        if (file != null)
            clientPath = file.getPath() + File.separator + "bin" + File.separator + "minecraft.jar";
        else
            clientPath = null;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Patcher patcher = new Patcher();
                patcher.setLocationRelativeTo(null);
                patcher.setVisible(true);
                patcher.refreshClientInfo(clientPath);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField clientPath;
    private javax.swing.JTextField clientState;
    private javax.swing.JTextField clientVersion;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextArea output;
    private javax.swing.JButton patchButton;
    // End of variables declaration//GEN-END:variables

    private String clientPatchKey = null;
    private Properties clientPatchProps = null;
    private Map<String,PatchFile> newPatchFiles;
    private Map<String,PatchFile> replacePatchFiles;
    private boolean needForce = false;

    private static Properties loadProperties(String path) {
        Properties props = new Properties();
        InputStreamReader r = new InputStreamReader(Patcher.class.getResourceAsStream(path));
        try {
            props.load(r);
        } catch (IOException e) {}
        return props;
    }

    private class PatchFile {
        String path;
        long newCrc;
        long oldCrc;
        private PatchFile(String path, long newCrc, long oldCrc) {
            this.path = path;
            this.newCrc = newCrc;
            this.oldCrc = oldCrc;
        }
    }


}
