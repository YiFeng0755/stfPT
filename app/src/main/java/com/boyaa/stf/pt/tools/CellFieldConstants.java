package com.boyaa.stf.pt.tools;

/**
 * Created by GuixiangGui on 2017/2/20.
 */
public enum CellFieldConstants {

    Time, TopActivity, AppMem, AppMemRatio, FreeMemory, AppCPU, Traffic, SendTraffic, ReceiveTraffic, Fps, Battery,
    Measurement;
    int rowNumber;
    int columnNumber;

    public void setRowColumn(int rn, int cn) {
        this.rowNumber = rn;
        this.columnNumber = cn;
    }

    public int getRow() {
        return this.rowNumber;
    }

    public int getColumn() {
        return this.columnNumber;
    }

}
