
(ns source-code-documentation.assemble.js)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def SCRIPTS "
function toggleCollapsible (collapsibleId) {
  collapsible = document.getElementById ( collapsibleId );
  if (collapsible.dataset.collapsed === 'true') {
     collapsible.dataset.collapsed = 'false';
  } else {
     collapsible.dataset.collapsed = 'true';
  }}")
