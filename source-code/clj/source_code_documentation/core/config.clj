
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
                                :*plain*       {:text-size :s :hide-marker? true :text-overflow :wrap}
                                :*source-code* {:text-size :s :collapsed? true :collapsible? true}})
