package pl.opole.uni.cs.unifDL.Filo.controller;

public class SharedData {
	private static boolean runFlag = true;

    public static boolean getRunFlag() {
        return runFlag;
    }

    public static void setRunFlag(boolean value) {
    	runFlag = value;
    }
}


