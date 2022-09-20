// Add "Show more" and "Show less" functionality to HTML sections on form:
//
// <div class="pasta-more-text">
//   <div class="pasta-more-content">
//     { HTML }
//   </div>
//   <div class="pasta-more">
//   </div>
// </div>
//
// The corresponding styles are in more.css.

function toggle_clamp(ev)
{
  const more_el = ev.target;
  const text_el = more_el.parentElement;
  const content_el = text_el.getElementsByTagName("div")[0];
  if (content_el.classList.contains("pasta-more-content")) {
    content_el.classList.remove("pasta-more-content");
    content_el.classList.add("pasta-more-content-all");
    more_el.classList.remove("pasta-more-show");
    more_el.classList.add("pasta-more-less-show");
  }
  else {
    content_el.classList.add("pasta-more-content");
    content_el.classList.remove("pasta-more-content-all");
    more_el.classList.add("pasta-more-show");
    more_el.classList.remove("pasta-more-less-show");
  }
}

for (const text_el of document.getElementsByClassName("pasta-more-text")) {
  const content_el = text_el.getElementsByClassName("pasta-more-content")[0];
  if (content_el.scrollHeight > content_el.clientHeight) {
    const more_el = text_el.getElementsByClassName("pasta-more")[0];
    more_el.classList.add("pasta-more-show");
  }
}

for (const el of document.getElementsByClassName("pasta-more")) {
  el.addEventListener("click", toggle_clamp);
}
