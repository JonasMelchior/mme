package com.github.jonasmelchior.mymme.protocol.s1ap;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import java.util.Arrays;
import java.util.List;

public interface S1apLibrary extends Library {
    S1apLibrary INSTANCE = Native.load("s1ap", S1apLibrary.class);

    // --- Basic Types ---

    public static class asn_struct_ctx_t extends Structure {
        public short phase;
        public short step;
        public int context;
        public Pointer ptr;
        public com.sun.jna.NativeLong left;

        @Override protected List<String> getFieldOrder() { return Arrays.asList("phase", "step", "context", "ptr", "left"); }
    }

    public static class OCTET_STRING extends Structure {
        public Pointer buf;
        public int size;
        public asn_struct_ctx_t _asn_ctx;

        @Override protected List<String> getFieldOrder() { return Arrays.asList("buf", "size", "_asn_ctx"); }
        
        public byte[] getBytes() {
            if (buf == null || size <= 0) return new byte[0];
            return buf.getByteArray(0, size);
        }
    }

    public static class BIT_STRING extends Structure {
        public Pointer buf;
        public int size;
        public int bits_unused;
        public asn_struct_ctx_t _asn_ctx;

        @Override protected List<String> getFieldOrder() { return Arrays.asList("buf", "size", "bits_unused", "_asn_ctx"); }

        public byte[] getBytes() {
            if (buf == null || size <= 0) return new byte[0];
            return buf.getByteArray(0, size);
        }
    }

    // --- Common IEs ---

    public static class S1ap_Global_ENB_ID extends Structure {
        public OCTET_STRING pLMNidentity;
        public S1ap_ENB_ID eNB_ID;
        public Pointer iE_Extensions;
        public asn_struct_ctx_t _asn_ctx;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("pLMNidentity", "eNB_ID", "iE_Extensions", "_asn_ctx"); }
    }

    public static class S1ap_ENB_ID extends Structure {
        public int present;
        public BIT_STRING macroENB_ID; // Simplified union as first choice is most common
        public asn_struct_ctx_t _asn_ctx;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("present", "macroENB_ID", "_asn_ctx"); }
    }

    public static class S1ap_TAI extends Structure {
        public OCTET_STRING pLMNidentity;
        public OCTET_STRING tAC;
        public Pointer iE_Extensions;
        public asn_struct_ctx_t _asn_ctx;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("pLMNidentity", "tAC", "iE_Extensions", "_asn_ctx"); }
    }

    public static class S1ap_EUTRAN_CGI extends Structure {
        public OCTET_STRING pLMNidentity;
        public BIT_STRING cell_ID;
        public Pointer iE_Extensions;
        public asn_struct_ctx_t _asn_ctx;
        @Override protected List<String> getFieldOrder() { return Arrays.asList("pLMNidentity", "cell_ID", "iE_Extensions", "_asn_ctx"); }
    }

    // --- Message Specific IEs ---

    public static class S1SetupRequestIEs extends Structure {
        public short presenceMask;
        public S1ap_Global_ENB_ID global_ENB_ID;
        public OCTET_STRING eNBname;
        public byte[] _padding = new byte[1024]; 
        @Override protected List<String> getFieldOrder() { return Arrays.asList("presenceMask", "global_ENB_ID", "eNBname", "_padding"); }
    }

    public static class S1SetupResponseIEs extends Structure {
        public short presenceMask;
        public OCTET_STRING mmEname;
        public byte[] _padding = new byte[1024];
        @Override protected List<String> getFieldOrder() { return Arrays.asList("presenceMask", "mmEname", "_padding"); }
    }

    public static class InitialUEMessageIEs extends Structure {
        public short presenceMask;
        public long eNB_UE_S1AP_ID;
        public OCTET_STRING nas_pdu;
        public S1ap_TAI tai;
        public S1ap_EUTRAN_CGI eutran_cgi;
        public long rrC_Establishment_Cause;
        public byte[] _padding = new byte[1024];
        @Override protected List<String> getFieldOrder() { 
            return Arrays.asList("presenceMask", "eNB_UE_S1AP_ID", "nas_pdu", "tai", "eutran_cgi", "rrC_Establishment_Cause", "_padding"); 
        }
    }

    public static class UplinkNASTransportIEs extends Structure {
        public short presenceMask;
        public long mme_ue_s1ap_id;
        public long enb_ue_s1ap_id;
        public OCTET_STRING nas_pdu;
        public S1ap_EUTRAN_CGI eutran_cgi;
        public S1ap_TAI tai;
        public asn_struct_ctx_t _asn_ctx;
        @Override protected List<String> getFieldOrder() { 
            return Arrays.asList("presenceMask", "mme_ue_s1ap_id", "enb_ue_s1ap_id", "nas_pdu", "eutran_cgi", "tai", "_asn_ctx"); 
        }
    }

    public static class HandoverRequiredIEs extends Structure {
        public short presenceMask;
        public long mme_ue_s1ap_id;
        public long enb_ue_s1ap_id;
        public long handoverType;
        public byte[] _padding = new byte[1024];
        @Override protected List<String> getFieldOrder() { return Arrays.asList("presenceMask", "mme_ue_s1ap_id", "enb_ue_s1ap_id", "handoverType", "_padding"); }
    }

    public static class S1ap_InitialContextSetupResponseIEs extends Structure {
        public short presenceMask;
        public long mme_ue_s1ap_id;
        public long enb_ue_s1ap_id;
        public byte[] _padding = new byte[1024];
        @Override protected List<String> getFieldOrder() { return Arrays.asList("presenceMask", "mme_ue_s1ap_id", "enb_ue_s1ap_id", "_padding"); }
    }

    // --- The Main Union ---

    public static class S1apMessageUnion extends Union {
        public S1SetupRequestIEs s1ap_S1SetupRequestIEs;
        public S1SetupResponseIEs s1ap_S1SetupResponseIEs;
        public InitialUEMessageIEs s1ap_InitialUEMessageIEs;
        public UplinkNASTransportIEs s1ap_UplinkNASTransportIEs;
        public HandoverRequiredIEs s1ap_HandoverRequiredIEs;
        public S1ap_InitialContextSetupResponseIEs s1ap_InitialContextSetupResponseIEs;
        // Padding to accommodate any S1AP message type handled in C
        public byte[] _exhaustive_padding = new byte[4096];
    }

    // --- Top-Level Struct ---

    public static class S1apMessage extends Structure {
        public long procedureCode; 
        public long criticality;   
        public byte direction;     
        public S1apMessageUnion msg;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("procedureCode", "criticality", "direction", "msg");
        }

        @Override
        public void read() {
            super.read();
            // Dispatch union field based on procedureCode and direction
            // Codes from S1ap-ProcedureCode.h
            if (procedureCode == 17) { // S1Setup
                if (direction == 1) msg.setType("s1ap_S1SetupRequestIEs");
                else if (direction == 2) msg.setType("s1ap_S1SetupResponseIEs");
            } else if (procedureCode == 12) { // InitialUEMessage
                msg.setType("s1ap_InitialUEMessageIEs");
            } else if (procedureCode == 13) { // UplinkNASTransport
                msg.setType("s1ap_UplinkNASTransportIEs");
            } else if (procedureCode == 0) { // HandoverPreparation (Required)
                msg.setType("s1ap_HandoverRequiredIEs");
            } else if (procedureCode == 9) { // InitialContextSetup
                if (direction == 2) msg.setType("s1ap_InitialContextSetupResponseIEs");
            }
            msg.read();
        }
    }

    // --- Native Methods ---

    int s1ap_decode_pdu(S1apMessage message, byte[] buffer, int length);

    int s1ap_mme_generate_s1_setup_response(
        Pointer[] out_buffer, 
        Pointer out_length,
        String mme_name,
        byte mcc1, byte mcc2, byte mcc3,
        byte mnc1, byte mnc2, byte mnc3,
        short mme_group_id,
        byte mme_code,
        byte relative_capacity);

    int s1ap_mme_generate_initial_context_setup_request(
        Pointer[] out_buffer, 
        Pointer out_length,
        int mme_ue_s1ap_id,
        int enb_ue_s1ap_id,
        byte[] sgw_ip,
        int sgw_teid);

    int s1ap_mme_generate_downlink_nas_transport(
        Pointer[] out_buffer, 
        Pointer out_length,
        int mme_ue_s1ap_id,
        int enb_ue_s1ap_id,
        byte[] nas_pdu,
        int nas_pdu_length);

    int s1ap_mme_generate_handover_request(
        Pointer[] out_buffer, 
        Pointer out_length,
        int mme_ue_s1ap_id,
        int handover_type,
        int cause,
        byte[] source_to_target_container,
        int container_length);
}
