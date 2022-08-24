function isOverflowed(content_el)
{
  return content_el.scrollHeight > content_el.clientHeight;
}

function more(ev)
{
  const more_el = ev.target;
  const text_el = more_el.parentElement;
  const content_el = text_el.getElementsByClassName("ridare-content")[0];
  content_el.classList.remove("ridare-content");
  more_el.style.display = "none";
  more_el.classList.remove('ridare-more-more')
  more_el.classList.add('ridare-more-less')
}

for (const text_el of document.getElementsByClassName("ridare-text")) {
  const content_el = text_el.getElementsByClassName("ridare-content")[0];
  if (isOverflowed(content_el)) {
    const more_el = text_el.getElementsByClassName("ridare-more")[0];
    more_el.style.display = "block";
    more_el.classList.add('ridare-more-more')
  }
}

for (const el of document.getElementsByClassName("ridare-more")) {
  el.addEventListener("click", more);
}
