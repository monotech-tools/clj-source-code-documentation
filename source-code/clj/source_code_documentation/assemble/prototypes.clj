
(ns source-code-documentation.assemble.prototypes
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn block-props-prototype
  ; @ignore
  ;
  ; @param (map) content-block
  ; {:text (maps in vector)(opt)
  ;  ...}
  ; @param (keyword) block-id
  ; @param (map) block-props
  ; {:collapsible? (boolean)(opt)
  ;  ...}
  ;
  ; @return (map)
  ; {:collapsible? (boolean)
  ;  ...}
  [{:keys [text]} _ {:keys [collapsible?] :as block-props}]
  (-> block-props (assoc :collapsible? (and (-> collapsible?)
                                            (-> text vector/not-empty?)))))
