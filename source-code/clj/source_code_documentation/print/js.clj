
(ns source-code-documentation.print.js)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def SCRIPTS "
function toggleCollapsible (collapsibleId) {
  collapsible = document.getElementById ( collapsibleId );
  if (collapsible.dataset.expanded === 'true') {
     collapsible.dataset.expanded = 'false';
  } else {
     collapsible.dataset.expanded = 'true';
  }}")
