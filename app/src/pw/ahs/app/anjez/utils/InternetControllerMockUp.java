/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.utils;

public class InternetControllerMockUp {
    private static String errorMsg = "";
    public static final String HOME_PAGE = "http://ahs.pw/app/anjez/index.php";

    private static int updateRound = 0;
    private static int feedbackRound = 0;

    public static String getErrorMsg() {
        return errorMsg;
    }

    public static String getLatestVersion() {
        updateRound = (updateRound + 1) % 4;

        switch (updateRound) {
            case 0:
                errorMsg = "Not OK!";
                return "";
            case 1:
                errorMsg = "Timeout";
                return "";
            case 2:
                errorMsg = "";
                return "1.0.0";
            default:
                errorMsg = "";
                return "1.0.1";
        }
    }

    public static boolean sendFeedback(String feedback) {
        feedbackRound = (feedbackRound + 1) % 3;

        switch (feedbackRound) {
            case 0:
                errorMsg = "Not OK!";
                return false;
            case 1:
                errorMsg = "Timeout";
                return false;
            default:
                errorMsg = "";
                return true;
        }
    }
}
