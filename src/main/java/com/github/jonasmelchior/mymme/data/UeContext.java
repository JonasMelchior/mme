package com.github.jonasmelchior.mymme.data;

import java.io.Serializable;

/**
 * Exhaustive UE Context representation for EMM and ECM states.
 * Based on 3GPP TS 24.301 (EMM) and TS 36.413 (ECM).
 */
public class UeContext implements Serializable {
    private String imsi;
    private long mmeUeS1apId;
    private int enbUeS1apId;
    private java.net.SocketAddress enbAddress;
    
    private EmmState emmState = EmmState.EMM_DEREGISTERED;
    private EcmState ecmState = EcmState.ECM_IDLE;

    // Security Context
    private byte[] rand;
    private byte[] autn;
    private byte[] xres;
    private byte[] kAsme;
    private byte[] kNasInt;
    private byte[] kNasEnc;
    private int ulNasCount = 0;
    private int dlNasCount = 0;

    /**
     * EPS Mobility Management (EMM) States (TS 24.301, 5.1.3.2)
     */
    public enum EmmState {
        EMM_NULL,
        EMM_DEREGISTERED,
        EMM_REGISTERED_INITIATED,
        EMM_REGISTERED,
        EMM_DEREGISTERED_INITIATED,
        EMM_TRACKING_AREA_UPDATING_INITIATED,
        EMM_SERVICE_REQUEST_INITIATED
    }

    /**
     * EPS Connection Management (ECM) States (TS 23.401, 4.6.3)
     */
    public enum EcmState {
        ECM_IDLE,
        ECM_CONNECTED
    }

    private byte[] ueSecurityCapabilities;

    // Location Information
    private String mcc;
    private String mnc;
    private int tac;
    private int cellId;
    private int ratType = 1004; // Default to E-UTRAN (TS 29.212)
    private ProcedureType currentProcedure;

    public enum ProcedureType {
        ATTACH,
        TAU,
        SERVICE_REQUEST,
        DETACH
    }

    // S11 Session Context
    private int mmeS11Teid;
    private int sgwS11Teid;
    private byte ebi = 5; // Default EBI
    private String apn = "internet";
    private byte[] sgwIp;
    private int sgwS1Uteid;

    // Getters and Setters
    public String getImsi() { return imsi; }
    public void setImsi(String imsi) { this.imsi = imsi; }
    public long getMmeUeS1apId() { return mmeUeS1apId; }
    public void setMmeUeS1apId(long mmeUeS1apId) { this.mmeUeS1apId = mmeUeS1apId; }
    public int getEnbUeS1apId() { return enbUeS1apId; }
    public void setEnbUeS1apId(int enbUeS1apId) { this.enbUeS1apId = enbUeS1apId; }
    public java.net.SocketAddress getEnbAddress() { return enbAddress; }
    public void setEnbAddress(java.net.SocketAddress enbAddress) { this.enbAddress = enbAddress; }
    public EmmState getEmmState() { return emmState; }
    public void setEmmState(EmmState emmState) { this.emmState = emmState; }
    public EcmState getEcmState() { return ecmState; }
    public void setEcmState(EcmState ecmState) { this.ecmState = ecmState; }

    public byte[] getRand() { return rand; }
    public void setRand(byte[] rand) { this.rand = rand; }
    public byte[] getAutn() { return autn; }
    public void setAutn(byte[] autn) { this.autn = autn; }
    public byte[] getXres() { return xres; }
    public void setXres(byte[] xres) { this.xres = xres; }
    public byte[] getkAsme() { return kAsme; }
    public void setkAsme(byte[] kAsme) { this.kAsme = kAsme; }
    public byte[] getkNasInt() { return kNasInt; }
    public void setkNasInt(byte[] kNasInt) { this.kNasInt = kNasInt; }
    public byte[] getkNasEnc() { return kNasEnc; }
    public void setkNasEnc(byte[] kNasEnc) { this.kNasEnc = kNasEnc; }
    public int getUlNasCount() { return ulNasCount; }
    public void setUlNasCount(int ulNasCount) { this.ulNasCount = ulNasCount; }
    public int getDlNasCount() { return dlNasCount; }
    public void setDlNasCount(int dlNasCount) { this.dlNasCount = dlNasCount; }
    public byte[] getUeSecurityCapabilities() { return ueSecurityCapabilities; }
    public void setUeSecurityCapabilities(byte[] ueSecurityCapabilities) { this.ueSecurityCapabilities = ueSecurityCapabilities; }

    public String getMcc() { return mcc; }
    public void setMcc(String mcc) { this.mcc = mcc; }
    public String getMnc() { return mnc; }
    public void setMnc(String mnc) { this.mnc = mnc; }
    public int getTac() { return tac; }
    public void setTac(int tac) { this.tac = tac; }
    public int getCellId() { return cellId; }
    public void setCellId(int cellId) { this.cellId = cellId; }
    public int getRatType() { return ratType; }
    public void setRatType(int ratType) { this.ratType = ratType; }
    public ProcedureType getCurrentProcedure() { return currentProcedure; }
    public void setCurrentProcedure(ProcedureType currentProcedure) { this.currentProcedure = currentProcedure; }

    public int getMmeS11Teid() { return mmeS11Teid; }
    public void setMmeS11Teid(int mmeS11Teid) { this.mmeS11Teid = mmeS11Teid; }
    public int getSgwS11Teid() { return sgwS11Teid; }
    public void setSgwS11Teid(int sgwS11Teid) { this.sgwS11Teid = sgwS11Teid; }
    public byte getEbi() { return ebi; }
    public void setEbi(byte ebi) { this.ebi = ebi; }
    public String getApn() { return apn; }
    public void setApn(String apn) { this.apn = apn; }
    public byte[] getSgwIp() { return sgwIp; }
    public void setSgwIp(byte[] sgwIp) { this.sgwIp = sgwIp; }
    public int getSgwS1Uteid() { return sgwS1Uteid; }
    public void setSgwS1Uteid(int sgwS1Uteid) { this.sgwS1Uteid = sgwS1Uteid; }
}
