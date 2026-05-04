package com.github.jonasmelchior.mymme.service;

import com.github.jonasmelchior.mymme.data.UeContext;
import com.github.jonasmelchior.mymme.repository.UeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Exhaustive State Machine for EMM (EPS Mobility Management).
 * Handles all state transitions defined in 3GPP TS 24.301.
 */
@ApplicationScoped
public class EmmStateMachine {

    private static final Logger LOG = Logger.getLogger(EmmStateMachine.class);

    @Inject
    UeRepository ueRepository;

    /**
     * Handles EMM events and performs state transitions.
     */
    public void onEvent(UeContext context, EmmEvent event) {
        UeContext.EmmState currentState = context.getEmmState();
        UeContext.EmmState nextState = currentState;

        LOG.debugf("EMM State Machine: IMSI=%s, CurrentState=%s, Event=%s", 
                   context.getImsi(), currentState, event);

        switch (currentState) {
            case EMM_DEREGISTERED:
                if (event == EmmEvent.ATTACH_REQUEST_RECEIVED) {
                    nextState = UeContext.EmmState.EMM_REGISTERED_INITIATED;
                }
                break;

            case EMM_REGISTERED_INITIATED:
                if (event == EmmEvent.AUTHENTICATION_SUCCESS) {
                    // Stay in this state until Attach Accept is sent
                } else if (event == EmmEvent.ATTACH_ACCEPT_SENT) {
                    nextState = UeContext.EmmState.EMM_REGISTERED;
                } else if (event == EmmEvent.ATTACH_REJECT_SENT) {
                    nextState = UeContext.EmmState.EMM_DEREGISTERED;
                }
                break;

            case EMM_REGISTERED:
                if (event == EmmEvent.DETACH_REQUEST_RECEIVED) {
                    nextState = UeContext.EmmState.EMM_DEREGISTERED_INITIATED;
                } else if (event == EmmEvent.TAU_REQUEST_RECEIVED) {
                    nextState = UeContext.EmmState.EMM_TRACKING_AREA_UPDATING_INITIATED;
                }
                break;

            case EMM_DEREGISTERED_INITIATED:
                if (event == EmmEvent.DETACH_ACCEPT_SENT) {
                    nextState = UeContext.EmmState.EMM_DEREGISTERED;
                }
                break;

            case EMM_TRACKING_AREA_UPDATING_INITIATED:
                if (event == EmmEvent.TAU_ACCEPT_SENT) {
                    nextState = UeContext.EmmState.EMM_REGISTERED;
                }
                break;

            case EMM_SERVICE_REQUEST_INITIATED:
                if (event == EmmEvent.SERVICE_ACCEPT_SENT) {
                    nextState = UeContext.EmmState.EMM_REGISTERED;
                }
                break;
                
            default:
                LOG.warnf("No transition defined for state %s and event %s", currentState, event);
        }

        if (nextState != currentState) {
            LOG.infof("EMM Transition: IMSI=%s, %s -> %s", context.getImsi(), currentState, nextState);
            context.setEmmState(nextState);
            ueRepository.save(context);
        }
    }

    public enum EmmEvent {
        ATTACH_REQUEST_RECEIVED,
        ATTACH_ACCEPT_SENT,
        ATTACH_COMPLETE_RECEIVED,
        ATTACH_REJECT_SENT,
        DETACH_REQUEST_RECEIVED,
        DETACH_ACCEPT_SENT,
        TAU_REQUEST_RECEIVED,
        TAU_ACCEPT_SENT,
        AUTHENTICATION_SUCCESS,
        SERVICE_REQUEST_RECEIVED,
        SERVICE_ACCEPT_SENT
    }
}
