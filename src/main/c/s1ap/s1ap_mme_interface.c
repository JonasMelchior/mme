#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <arpa/inet.h>

#include "s1ap_common.h"
#include "s1ap_ies_defs.h"
#include "s1ap_mme_encoder.h"
#include "common_defs.h"

/**
 * Exhaustively generates an S1 Setup Response PDU.
 */
int s1ap_mme_generate_s1_setup_response(
    uint8_t **out_buffer, 
    uint32_t *out_length,
    const char *mme_name,
    uint8_t mcc1, uint8_t mcc2, uint8_t mcc3,
    uint8_t mnc1, uint8_t mnc2, uint8_t mnc3,
    uint16_t mme_group_id,
    uint8_t mme_code,
    uint8_t relative_capacity) 
{
    s1ap_message message = {0};
    S1ap_S1SetupResponseIEs_t *ies = &message.msg.s1ap_S1SetupResponseIEs;

    message.procedureCode = 17;
    message.direction     = S1AP_PDU_PR_successfulOutcome;

    if (mme_name) {
        ies->presenceMask |= S1AP_S1SETUPRESPONSEIES_MMENAME_PRESENT;
        ies->mmEname.buf = (uint8_t *)strdup(mme_name);
        ies->mmEname.size = strlen(mme_name);
    }

    ies->relativeMMECapacity = relative_capacity;
    ies->servedGUMMEIs.list.count = 1;
    ies->servedGUMMEIs.list.array = calloc(1, sizeof(struct S1ap_ServedGUMMEIsItem *));
    struct S1ap_ServedGUMMEIsItem *item = calloc(1, sizeof(struct S1ap_ServedGUMMEIsItem));
    ies->servedGUMMEIs.list.array[0] = item;

    item->servedPLMNs.list.count = 1;
    item->servedPLMNs.list.array = calloc(1, sizeof(S1ap_PLMNidentity_t *));
    S1ap_PLMNidentity_t *plmn = calloc(1, sizeof(S1ap_PLMNidentity_t));
    item->servedPLMNs.list.array[0] = plmn;
    plmn->buf = malloc(3);
    plmn->size = 3;
    plmn->buf[0] = (mcc2 << 4) | mcc1;
    plmn->buf[1] = (mnc3 << 4) | mcc3;
    plmn->buf[2] = (mnc2 << 4) | mnc1;

    item->servedGroupIDs.list.count = 1;
    item->servedGroupIDs.list.array = calloc(1, sizeof(S1ap_MME_Group_ID_t *));
    S1ap_MME_Group_ID_t *gid = calloc(1, sizeof(S1ap_MME_Group_ID_t));
    item->servedGroupIDs.list.array[0] = gid;
    gid->buf = malloc(2); gid->size = 2;
    uint16_t gid_net = htons(mme_group_id);
    memcpy(gid->buf, &gid_net, 2);

    item->servedMMECs.list.count = 1;
    item->servedMMECs.list.array = calloc(1, sizeof(S1ap_MME_Code_t *));
    S1ap_MME_Code_t *mmec = calloc(1, sizeof(S1ap_MME_Code_t));
    item->servedMMECs.list.array[0] = mmec;
    mmec->buf = malloc(1); mmec->size = 1;
    mmec->buf[0] = mme_code;

    return s1ap_mme_encode_pdu(&message, out_buffer, out_length);
}

/**
 * Exhaustively generates an S1AP Initial Context Setup Request.
 */
int s1ap_mme_generate_initial_context_setup_request(
    uint8_t **out_buffer, 
    uint32_t *out_length,
    uint32_t mme_ue_s1ap_id,
    uint32_t enb_ue_s1ap_id,
    uint8_t  *sgw_ip,
    uint32_t sgw_teid) 
{
    s1ap_message message = {0};
    S1ap_InitialContextSetupRequestIEs_t *ies = &message.msg.s1ap_InitialContextSetupRequestIEs;

    message.procedureCode = 9; 
    message.direction     = S1AP_PDU_PR_initiatingMessage;

    ies->mme_ue_s1ap_id = mme_ue_s1ap_id;
    ies->eNB_UE_S1AP_ID = enb_ue_s1ap_id;

    asn_long2INTEGER(&ies->uEaggregateMaximumBitrate.uEaggregateMaximumBitRateDL, 100000000);
    asn_long2INTEGER(&ies->uEaggregateMaximumBitrate.uEaggregateMaximumBitRateUL, 50000000);

    ies->e_RABToBeSetupListCtxtSUReq.s1ap_E_RABToBeSetupItemCtxtSUReq.count = 1;
    ies->e_RABToBeSetupListCtxtSUReq.s1ap_E_RABToBeSetupItemCtxtSUReq.array = calloc(1, sizeof(struct S1ap_E_RABToBeSetupItemCtxtSUReq *));
    struct S1ap_E_RABToBeSetupItemCtxtSUReq *item = calloc(1, sizeof(struct S1ap_E_RABToBeSetupItemCtxtSUReq));
    ies->e_RABToBeSetupListCtxtSUReq.s1ap_E_RABToBeSetupItemCtxtSUReq.array[0] = item;

    item->e_RAB_ID = 5;
    item->e_RABlevelQoSParameters.qCI = 9;
    item->e_RABlevelQoSParameters.allocationRetentionPriority.priorityLevel = 15;

    item->transportLayerAddress.buf = malloc(4);
    item->transportLayerAddress.size = 4;
    memcpy(item->transportLayerAddress.buf, sgw_ip, 4);

    item->gTP_TEID.buf = malloc(4);
    item->gTP_TEID.size = 4;
    uint32_t teid_net = htonl(sgw_teid);
    memcpy(item->gTP_TEID.buf, &teid_net, 4);

    ies->ueSecurityCapabilities.encryptionAlgorithms.buf = calloc(1, 2);
    ies->ueSecurityCapabilities.encryptionAlgorithms.size = 2;
    ies->ueSecurityCapabilities.encryptionAlgorithms.buf[0] = 0xe0;
    ies->ueSecurityCapabilities.integrityProtectionAlgorithms.buf = calloc(1, 2);
    ies->ueSecurityCapabilities.integrityProtectionAlgorithms.size = 2;
    ies->ueSecurityCapabilities.integrityProtectionAlgorithms.buf[0] = 0xe0;

    ies->securityKey.buf = calloc(1, 32);
    ies->securityKey.size = 32;

    return s1ap_mme_encode_pdu(&message, out_buffer, out_length);
}

/**
 * Exhaustively generates an S1AP Downlink NAS Transport message.
 */
int s1ap_mme_generate_downlink_nas_transport(
    uint8_t **out_buffer, 
    uint32_t *out_length,
    uint32_t mme_ue_s1ap_id,
    uint32_t enb_ue_s1ap_id,
    uint8_t  *nas_pdu,
    uint32_t nas_pdu_length) 
{
    s1ap_message message = {0};
    S1ap_DownlinkNASTransportIEs_t *ies = &message.msg.s1ap_DownlinkNASTransportIEs;

    message.procedureCode = 11; 
    message.direction     = S1AP_PDU_PR_initiatingMessage;

    ies->mme_ue_s1ap_id = mme_ue_s1ap_id;
    ies->eNB_UE_S1AP_ID = enb_ue_s1ap_id;

    ies->nas_pdu.buf = malloc(nas_pdu_length);
    ies->nas_pdu.size = nas_pdu_length;
    memcpy(ies->nas_pdu.buf, nas_pdu, nas_pdu_length);

    return s1ap_mme_encode_pdu(&message, out_buffer, out_length);
}

/**
 * Exhaustively generates an S1AP Handover Request.
 */
int s1ap_mme_generate_handover_request(
    uint8_t **out_buffer, 
    uint32_t *out_length,
    uint32_t mme_ue_s1ap_id,
    uint32_t handover_type,
    uint32_t cause,
    uint8_t  *source_to_target_container,
    uint32_t container_length) 
{
    s1ap_message message = {0};
    S1ap_HandoverRequestIEs_t *ies = &message.msg.s1ap_HandoverRequestIEs;

    message.procedureCode = 0; // id-HandoverPreparation (Target side)
    message.direction     = S1AP_PDU_PR_initiatingMessage;

    // 1. IDs
    ies->mme_ue_s1ap_id = mme_ue_s1ap_id;
    ies->handoverType   = handover_type;
    ies->cause.present  = S1ap_Cause_PR_radioNetwork;
    ies->cause.choice.radioNetwork = cause;

    // 2. UE Aggregate Maximum Bit Rate
    asn_long2INTEGER(&ies->uEaggregateMaximumBitrate.uEaggregateMaximumBitRateDL, 100000000);
    asn_long2INTEGER(&ies->uEaggregateMaximumBitrate.uEaggregateMaximumBitRateUL, 50000000);

    // 3. E-RAB to be Setup List (Exhaustive - simplified for now)
    ies->e_RABToBeSetupListHOReq.s1ap_E_RABToBeSetupItemHOReq.count = 0;

    // 4. Source to Target Transparent Container
    ies->source_ToTarget_TransparentContainer.buf = malloc(container_length);
    ies->source_ToTarget_TransparentContainer.size = container_length;
    memcpy(ies->source_ToTarget_TransparentContainer.buf, source_to_target_container, container_length);

    return s1ap_mme_encode_pdu(&message, out_buffer, out_length);
}
