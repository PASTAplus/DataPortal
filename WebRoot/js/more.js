//     <div class="ridare-text">
//       <div class="ridare-content">
//         {}
//       </div>
//       <div class="ridare-more">
//       </div>
//     </div>

const MAX_HEIGHT = 30;

function isOverflowed(content_el)
{
  // return content_el.scrollHeight > content_el.clientHeight;
  return content_el.clientHeight > MAX_HEIGHT;
}

function toggle_clamp(text_el)
{
  const content_el = text_el.getElementsByClassName("ridare-content")[0];
  const more_el = text_el.getElementsByClassName("ridare-more")[0];
  if (content_el.classList.contains("ridare-clamp")) {
    content_el.classList.remove("ridare-clamp");
    more_el.classList.remove("ridare-more-show");
    more_el.classList.add("ridare-less-show");
  }
  else {
    content_el.classList.add("ridare-clamp");
    more_el.classList.add("ridare-more-show");
    more_el.classList.remove("ridare-less-show");
  }
}

function toggle_clamp_ev(ev)
{
  const more_el = ev.target;
  const text_el = more_el.parentElement;
  toggle_clamp(text_el);
}

for (const text_el of document.getElementsByClassName("ridare-text")) {
  const content_el = text_el.getElementsByClassName("ridare-content")[0];
  if (isOverflowed(content_el)) {
    toggle_clamp(text_el);
  }
  // else {
  // }
  // more_el.style.display = "block";
  // more_el.style.display = "none";
}

for (const el of document.getElementsByClassName("ridare-more")) {
  el.addEventListener("click", toggle_clamp_ev);
}
