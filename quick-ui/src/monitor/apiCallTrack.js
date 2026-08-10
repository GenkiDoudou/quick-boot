/**
 * @deprecated 请使用 requestObservation.js
 */
export {
  registerObservationEmitter,
  registerObservationEmitter as registerApiCallTrack,
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationSuccess as recordApiSuccess,
  finalizeRequestObservationError,
  finalizeRequestObservationError as recordApiError
} from './requestObservation'
