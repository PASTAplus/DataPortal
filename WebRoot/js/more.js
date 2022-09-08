// Structure of HTML fragment returned from Ridare:
//
// <div class="ridare-text">
//   <div class="ridare-content">
//     { HTML }
//   </div>
//   <div class="ridare-more">
//   </div>
// </div>

function toggle_clamp(ev)
{
  const more_el = ev.target;
  const text_el = more_el.parentElement;
  const content_el = text_el.getElementsByTagName("div")[0];
  if (content_el.classList.contains("ridare-content")) {
    content_el.classList.remove("ridare-content");
    content_el.classList.add("ridare-content-all");
    more_el.classList.remove("ridare-more-show");
    more_el.classList.add("ridare-less-show");
  }
  else {
    content_el.classList.add("ridare-content");
    content_el.classList.remove("ridare-content-all");
    more_el.classList.add("ridare-more-show");
    more_el.classList.remove("ridare-less-show");
  }
}

for (const text_el of document.getElementsByClassName("ridare-text")) {
  const content_el = text_el.getElementsByClassName("ridare-content")[0];
  if (content_el.scrollHeight > content_el.clientHeight) {
    const more_el = text_el.getElementsByClassName("ridare-more")[0];
    more_el.classList.add("ridare-more-show");
  }
}

for (const el of document.getElementsByClassName("ridare-more")) {
  el.addEventListener("click", toggle_clamp);
}
