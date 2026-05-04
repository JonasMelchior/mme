#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>

#include "s1ap_common.h"
#include "s1ap_ies_defs.h"

// --- Initiating Message Dispatcher ---
static int s1ap_decode_initiating_msg(s1ap_message *message, S1ap_InitiatingMessage_t *initiating) {
    int ret = -1;
    message->procedureCode = initiating->procedureCode;
    message->criticality   = initiating->criticality;

    switch (initiating->procedureCode) {
        case S1ap_ProcedureCode_id_HandoverPreparation:
            ret = s1ap_decode_s1ap_handoverrequiredies(&message->msg.s1ap_HandoverRequiredIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_HandoverResourceAllocation:
            ret = s1ap_decode_s1ap_handoverrequesties(&message->msg.s1ap_HandoverRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_HandoverNotification:
            ret = s1ap_decode_s1ap_handovernotifyies(&message->msg.s1ap_HandoverNotifyIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_PathSwitchRequest:
            ret = s1ap_decode_s1ap_pathswitchrequesties(&message->msg.s1ap_PathSwitchRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_HandoverCancel:
            ret = s1ap_decode_s1ap_handovercancelies(&message->msg.s1ap_HandoverCancelIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_E_RABSetup:
            ret = s1ap_decode_s1ap_e_rabsetuprequesties(&message->msg.s1ap_E_RABSetupRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_E_RABModify:
            ret = s1ap_decode_s1ap_e_rabmodifyrequesties(&message->msg.s1ap_E_RABModifyRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_E_RABRelease:
            ret = s1ap_decode_s1ap_e_rabreleasecommandies(&message->msg.s1ap_E_RABReleaseCommandIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_E_RABReleaseIndication:
            ret = s1ap_decode_s1ap_e_rabreleaseindicationies(&message->msg.s1ap_E_RABReleaseIndicationIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_InitialContextSetup:
            ret = s1ap_decode_s1ap_initialcontextsetuprequesties(&message->msg.s1ap_InitialContextSetupRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_Paging:
            ret = s1ap_decode_s1ap_pagingies(&message->msg.s1ap_PagingIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_downlinkNASTransport:
            ret = s1ap_decode_s1ap_downlinknastransporties(&message->msg.s1ap_DownlinkNASTransportIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_initialUEMessage:
            ret = s1ap_decode_s1ap_initialuemessageies(&message->msg.s1ap_InitialUEMessageIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_uplinkNASTransport:
            ret = s1ap_decode_s1ap_uplinknastransporties(&message->msg.s1ap_UplinkNASTransportIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_Reset:
            ret = s1ap_decode_s1ap_reseties(&message->msg.s1ap_ResetIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_ErrorIndication:
            ret = s1ap_decode_s1ap_errorindicationies(&message->msg.s1ap_ErrorIndicationIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_NASNonDeliveryIndication:
            ret = s1ap_decode_s1ap_nasnondeliveryindication_ies(&message->msg.s1ap_NASNonDeliveryIndication_IEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_S1Setup:
            ret = s1ap_decode_s1ap_s1setuprequesties(&message->msg.s1ap_S1SetupRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_UEContextReleaseRequest:
            ret = s1ap_decode_s1ap_uecontextreleaserequesties(&message->msg.s1ap_UEContextReleaseRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_DownlinkS1cdma2000tunneling:
            ret = s1ap_decode_s1ap_downlinks1cdma2000tunnelingies(&message->msg.s1ap_DownlinkS1cdma2000tunnelingIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_UplinkS1cdma2000tunneling:
            ret = s1ap_decode_s1ap_uplinks1cdma2000tunnelingies(&message->msg.s1ap_UplinkS1cdma2000tunnelingIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_UEContextModification:
            ret = s1ap_decode_s1ap_uecontextmodificationrequesties(&message->msg.s1ap_UEContextModificationRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_UECapabilityInfoIndication:
            ret = s1ap_decode_s1ap_uecapabilityinfoindicationies(&message->msg.s1ap_UECapabilityInfoIndicationIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_UEContextRelease:
            ret = s1ap_decode_s1ap_uecontextreleasecommandies(&message->msg.s1ap_UEContextReleaseCommandIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_eNBStatusTransfer:
            ret = s1ap_decode_s1ap_enbstatustransferies(&message->msg.s1ap_ENBStatusTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_MMEStatusTransfer:
            ret = s1ap_decode_s1ap_mmestatustransferies(&message->msg.s1ap_MMEStatusTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_DeactivateTrace:
            ret = s1ap_decode_s1ap_deactivatetraceies(&message->msg.s1ap_DeactivateTraceIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_TraceStart:
            ret = s1ap_decode_s1ap_tracestarties(&message->msg.s1ap_TraceStartIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_TraceFailureIndication:
            ret = s1ap_decode_s1ap_tracefailureindicationies(&message->msg.s1ap_TraceFailureIndicationIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_ENBConfigurationUpdate:
            ret = s1ap_decode_s1ap_enbconfigurationupdateies(&message->msg.s1ap_ENBConfigurationUpdateIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_MMEConfigurationUpdate:
            ret = s1ap_decode_s1ap_mmeconfigurationupdateies(&message->msg.s1ap_MMEConfigurationUpdateIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_LocationReportingControl:
            ret = s1ap_decode_s1ap_locationreportingcontrolies(&message->msg.s1ap_LocationReportingControlIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_LocationReportingFailureIndication:
            ret = s1ap_decode_s1ap_locationreportingfailureindicationies(&message->msg.s1ap_LocationReportingFailureIndicationIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_LocationReport:
            ret = s1ap_decode_s1ap_locationreporties(&message->msg.s1ap_LocationReportIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_OverloadStart:
            ret = s1ap_decode_s1ap_overloadstarties(&message->msg.s1ap_OverloadStartIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_OverloadStop:
            ret = s1ap_decode_s1ap_overloadstopies(&message->msg.s1ap_OverloadStopIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_WriteReplaceWarning:
            ret = s1ap_decode_s1ap_writereplacewarningrequesties(&message->msg.s1ap_WriteReplaceWarningRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_eNBDirectInformationTransfer:
            ret = s1ap_decode_s1ap_enbdirectinformationtransferies(&message->msg.s1ap_ENBDirectInformationTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_MMEDirectInformationTransfer:
            ret = s1ap_decode_s1ap_mmedirectinformationtransferies(&message->msg.s1ap_MMEDirectInformationTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_eNBConfigurationTransfer:
            ret = s1ap_decode_s1ap_enbconfigurationtransferies(&message->msg.s1ap_ENBConfigurationTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_MMEConfigurationTransfer:
            ret = s1ap_decode_s1ap_mmeconfigurationtransferies(&message->msg.s1ap_MMEConfigurationTransferIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_CellTrafficTrace:
            ret = s1ap_decode_s1ap_celltraffictraceies(&message->msg.s1ap_CellTrafficTraceIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_Kill:
            ret = s1ap_decode_s1ap_killrequesties(&message->msg.s1ap_KillRequestIEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_downlinkUEAssociatedLPPaTransport:
            ret = s1ap_decode_s1ap_downlinkueassociatedlppatransport_ies(&message->msg.s1ap_DownlinkUEAssociatedLPPaTransport_IEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_uplinkUEAssociatedLPPaTransport:
            ret = s1ap_decode_s1ap_uplinkueassociatedlppatransport_ies(&message->msg.s1ap_UplinkUEAssociatedLPPaTransport_IEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_downlinkNonUEAssociatedLPPaTransport:
            ret = s1ap_decode_s1ap_downlinknonueassociatedlppatransport_ies(&message->msg.s1ap_DownlinkNonUEAssociatedLPPaTransport_IEs, &initiating->value);
            break;
        case S1ap_ProcedureCode_id_uplinkNonUEAssociatedLPPaTransport:
            ret = s1ap_decode_s1ap_uplinknonueassociatedlppatransport_ies(&message->msg.s1ap_UplinkNonUEAssociatedLPPaTransport_IEs, &initiating->value);
            break;
        default:
            fprintf(stderr, "Wrapper: Unsupported initiating procedure %ld\n", initiating->procedureCode);
            break;
    }
    return ret;
}

// --- Successful Outcome Dispatcher ---
static int s1ap_decode_successful_msg(s1ap_message *message, S1ap_SuccessfulOutcome_t *successful) {
    int ret = -1;
    message->procedureCode = successful->procedureCode;
    message->criticality   = successful->criticality;

    switch (successful->procedureCode) {
        case S1ap_ProcedureCode_id_HandoverPreparation:
            ret = s1ap_decode_s1ap_handovercommandies(&message->msg.s1ap_HandoverCommandIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_HandoverResourceAllocation:
            ret = s1ap_decode_s1ap_handoverrequestacknowledgeies(&message->msg.s1ap_HandoverRequestAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_PathSwitchRequest:
            ret = s1ap_decode_s1ap_pathswitchrequestacknowledgeies(&message->msg.s1ap_PathSwitchRequestAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_HandoverCancel:
            ret = s1ap_decode_s1ap_handovercancelacknowledgeies(&message->msg.s1ap_HandoverCancelAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_E_RABSetup:
            ret = s1ap_decode_s1ap_e_rabsetupresponseies(&message->msg.s1ap_E_RABSetupResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_E_RABModify:
            ret = s1ap_decode_s1ap_e_rabmodifyresponseies(&message->msg.s1ap_E_RABModifyResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_E_RABRelease:
            ret = s1ap_decode_s1ap_e_rabreleaseresponseies(&message->msg.s1ap_E_RABReleaseResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_InitialContextSetup:
            ret = s1ap_decode_s1ap_initialcontextsetupresponseies(&message->msg.s1ap_InitialContextSetupResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_Reset:
            ret = s1ap_decode_s1ap_resetacknowledgeies(&message->msg.s1ap_ResetAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_S1Setup:
            ret = s1ap_decode_s1ap_s1setupresponseies(&message->msg.s1ap_S1SetupResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_UEContextModification:
            ret = s1ap_decode_s1ap_uecontextmodificationresponseies(&message->msg.s1ap_UEContextModificationResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_UEContextRelease:
            ret = s1ap_decode_s1ap_uecontextreleasecompleteies(&message->msg.s1ap_UEContextReleaseCompleteIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_ENBConfigurationUpdate:
            ret = s1ap_decode_s1ap_enbconfigurationupdateacknowledgeies(&message->msg.s1ap_ENBConfigurationUpdateAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_MMEConfigurationUpdate:
            ret = s1ap_decode_s1ap_mmeconfigurationupdateacknowledgeies(&message->msg.s1ap_MMEConfigurationUpdateAcknowledgeIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_WriteReplaceWarning:
            ret = s1ap_decode_s1ap_writereplacewarningresponseies(&message->msg.s1ap_WriteReplaceWarningResponseIEs, &successful->value);
            break;
        case S1ap_ProcedureCode_id_Kill:
            ret = s1ap_decode_s1ap_killresponseies(&message->msg.s1ap_KillResponseIEs, &successful->value);
            break;
        default:
            fprintf(stderr, "Wrapper: Unsupported successful procedure %ld\n", successful->procedureCode);
            break;
    }
    return ret;
}

// --- Unsuccessful Outcome Dispatcher ---
static int s1ap_decode_unsuccessful_msg(s1ap_message *message, S1ap_UnsuccessfulOutcome_t *unsuccessful) {
    int ret = -1;
    message->procedureCode = unsuccessful->procedureCode;
    message->criticality   = unsuccessful->criticality;

    switch (unsuccessful->procedureCode) {
        case S1ap_ProcedureCode_id_HandoverPreparation:
            ret = s1ap_decode_s1ap_handoverpreparationfailureies(&message->msg.s1ap_HandoverPreparationFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_HandoverResourceAllocation:
            ret = s1ap_decode_s1ap_handoverfailureies(&message->msg.s1ap_HandoverFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_PathSwitchRequest:
            ret = s1ap_decode_s1ap_pathswitchrequestfailureies(&message->msg.s1ap_PathSwitchRequestFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_InitialContextSetup:
            ret = s1ap_decode_s1ap_initialcontextsetupfailureies(&message->msg.s1ap_InitialContextSetupFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_S1Setup:
            ret = s1ap_decode_s1ap_s1setupfailureies(&message->msg.s1ap_S1SetupFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_UEContextModification:
            ret = s1ap_decode_s1ap_uecontextmodificationfailureies(&message->msg.s1ap_UEContextModificationFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_ENBConfigurationUpdate:
            ret = s1ap_decode_s1ap_enbconfigurationupdatefailureies(&message->msg.s1ap_ENBConfigurationUpdateFailureIEs, &unsuccessful->value);
            break;
        case S1ap_ProcedureCode_id_MMEConfigurationUpdate:
            ret = s1ap_decode_s1ap_mmeconfigurationupdatefailureies(&message->msg.s1ap_MMEConfigurationUpdateFailureIEs, &unsuccessful->value);
            break;
        default:
            fprintf(stderr, "Wrapper: Unsupported unsuccessful procedure %ld\n", unsuccessful->procedureCode);
            break;
    }
    return ret;
}

int s1ap_decode_pdu(s1ap_message *message, const uint8_t *const buffer, const uint32_t length) {
    S1AP_PDU_t *pdu = NULL;
    asn_dec_rval_t dec_rval;

    memset(message, 0, sizeof(s1ap_message));

    dec_rval = aper_decode(NULL, &asn_DEF_S1AP_PDU, (void **)&pdu, buffer, length, 0, 0);

    if (dec_rval.code != RC_OK) {
        if (pdu) ASN_STRUCT_FREE(asn_DEF_S1AP_PDU, pdu);
        return -1;
    }

    int ret = -1;
    message->direction = pdu->present;

    switch (pdu->present) {
        case S1AP_PDU_PR_initiatingMessage:
            ret = s1ap_decode_initiating_msg(message, &pdu->choice.initiatingMessage);
            break;
        case S1AP_PDU_PR_successfulOutcome:
            ret = s1ap_decode_successful_msg(message, &pdu->choice.successfulOutcome);
            break;
        case S1AP_PDU_PR_unsuccessfulOutcome:
            ret = s1ap_decode_unsuccessful_msg(message, &pdu->choice.unsuccessfulOutcome);
            break;
        default:
            fprintf(stderr, "Wrapper: Unsupported PDU type %d\n", pdu->present);
            break;
    }

    if (pdu) ASN_STRUCT_FREE(asn_DEF_S1AP_PDU, pdu);
    return ret;
}
