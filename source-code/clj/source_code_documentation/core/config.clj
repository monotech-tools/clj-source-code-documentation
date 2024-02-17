
(ns source-code-documentation.core.config)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (regex pattern)
(def SOURCE-FILENAME-PATTERN #"[a-z\_\d]+\.clj[cs]?")

; @ignore
;
; @constant (regex pattern)
(def DEFAULT-FILENAME-PATTERN SOURCE-FILENAME-PATTERN)

; @ignore
;
; @constant (map)
(def PREDEFINED-SNIPPET-CONFIG {:*error*       {:text-size :s :marker-color :warning}
                                :*plain*       {:text-size :s :text-overflow :wrap :hide-marker? true}
                                :*source-code* {:text-size :s :text-overflow :scroll :collapsed? true :collapsible? true}})
