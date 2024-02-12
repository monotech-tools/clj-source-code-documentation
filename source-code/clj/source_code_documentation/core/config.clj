
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
(def PREDEFINED-SNIPPET-CONFIG {:*error*       {:marker-color :warning}
                                :*plain*       {:hide-marker? true :text-overflow :wrap}
                                :*source-code* {:collapsed? true :collapsible? true}})
