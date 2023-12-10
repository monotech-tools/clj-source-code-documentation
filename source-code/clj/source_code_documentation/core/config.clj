
(ns source-code-documentation.core.config)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (regex pattern)
(def SOURCE-FILENAME-PATTERN #"[a-z\_\d]{1,}\.clj[cs]{0,1}")

; @ignore
;
; @constant (regex pattern)
(def DEFAULT-FILENAME-PATTERN SOURCE-FILENAME-PATTERN)
