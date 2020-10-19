/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

import javax.swing.*;

class Launcher {
    public static void main(String[] args) {
        if (!System.getProperty("java.version").startsWith("1.8")) {
            System.err.println("At least Java 1.8.0 is required");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            String msg;
            String title;
            if (System.getProperty("user.language").startsWith("ar")) {
                msg = "هذا التطبيق يحتاج لجافا 1.8.0 كحد أدنى! عذراً للإزعاج.";
                title = "خطأ";
            } else {
                msg = "This app requires at least Java 1.8.0.\nSorry for the inconvenience.";
                title = "Error";
            }
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        pw.ahs.app.anjez.gui.App.main(args);
    }
}
