/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.gwt4nb;

import java.awt.Frame;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Adds a reference to a GWT module to a HTML file.
 */
public class UseModuleCodeGenerator implements CodeGenerator {
    private JTextComponent textComp;
    private List<String> modules;

    /**
     * @param textComp text component
     * @param modules GWT modules defined (cannot be empty)
     */
    private UseModuleCodeGenerator(JTextComponent textComp, 
            List<String> modules) {
        this.modules = modules;
        this.textComp = textComp;
    }

    public static class Factory implements CodeGenerator.Factory {
        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent textComp = context.lookup(JTextComponent.class);
            List<? extends CodeGenerator> r = null;
            FileObject fo =
                    NbEditorUtilities.getFileObject(textComp.getDocument());
            if (fo != null) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    GWTProjectInfo pi =
                            GWTProjectInfo.get(project);
                    if (pi != null) {
                        List<String> m = pi.getModules();
                        if (m.size() > 0)
                            r = Collections.singletonList(
                                    new UseModuleCodeGenerator(textComp, m));
                    }
                }
            }
            if (r == null)
                r = Collections.emptyList();
            return r;
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UseModuleCodeGenerator.class, "UseMod"); // NOI18N
    }

    public void invoke() {
        Document doc = textComp.getDocument();
        try {
            Frame mainWindow =
                    WindowManager.getDefault().getMainWindow();
            String module;
            if (modules.size() > 1)
                module = (String) JOptionPane.showInputDialog(
                    mainWindow, NbBundle.getMessage(UseModuleCodeGenerator.class, 
                    "M"), // NOI18N
                    NbBundle.getMessage(UseModuleCodeGenerator.class, 
                    "ChooseMod"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE, null, 
                    modules.toArray(new String[modules.size()]),
                    modules.get(0));
            else
                module = modules.get(0);
            if (module != null)
                doc.insertString(textComp.getCaretPosition(),
                        "<script type=\"text/javascript\" src=\"" + module + // NOI18N
                        "/" + module + ".nocache.js\"></script>", null); // NOI18N
        } catch (BadLocationException ex) {
            GWT4NBUtil.unexpectedException(ex);
        }
    }
}
