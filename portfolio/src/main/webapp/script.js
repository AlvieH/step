// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
const AUDIO_LOCATION_ = "/files/audio/"
/**
 * Adds a random greeting to the page.
 */
const addRandomGreeting = () => {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
* Plays a specified melody
 */
const play = (melody) => {
  const sound = new Audio(AUDIO_LOCATION_ + melody + ".mp3");
  sound.play();
}

/*
* Slides shown element up given id and animation duration in ms
*/
const slideUp = (elementId, duration) => {
  var element = document.getElementById(elementId);

  /* set element up to be animated */
  element.style.transitionProperty = 'height, margin, padding';
  element.style.transitionDuration = duration + 'ms';
  element.style.boxSizing = 'border-box';
  element.style.height = element.offsetHeight + 'px';

  /* switch state to hidden - this way the transition will be animated */
  element.offsetHeight;
  element.style.height = 0;
  element.style.paddingTop = 0;
  element.style.paddingBottom = 0;
  element.style.marginTop = 0;
  element.style.marginBottom = 0;
  element.style.overflow = 'hidden';

  /* reset element and set display to none */
  window.setTimeout( () => {
    element.style.display = 'none';
    element.style.removeProperty('height');
    element.style.removeProperty('padding-top');
    element.style.removeProperty('padding-bottom');
    element.style.removeProperty('margin-top');
    element.style.removeProperty('margin-bottom');
    element.style.removeProperty('overflow');
    element.style.removeProperty('transition-property');
    element.style.removeProperty('transition-duration');
  }, duration);
}

/* Slide hidden element with given ID down, lasting duration ms */
const slideDown = (elementId, duration) => {
  var element = document.getElementById(elementId);

  /* resetting element's display properties to block */
  element.style.removeProperty('display');
  var display = window.getComputedStyle(element).display;
  if (display === 'none'){
    display = 'block';
  }
  element.style.display = display;

  /* resetting element's style */
  const height = element.offsetHeight;
  hideEl(element);
  element.offsetHeight;

  /* slide logic */
  element.style.boxSizing = 'border-box';
  element.style.transitionProperty = 'height, margin, padding';
  element.style.transitionDuration = duration + 'ms';
  element.style.height = height + 'px';
  element.style.removeProperty('padding-top');
  element.style.removeProperty('padding-bottom');
  element.style.removeProperty('margin-top');
  element.style.removeProperty('margin-bottom');

  /* remove unnecessary properties after animation completion */
  window.setTimeout( () => {
    element.style.removeProperty('height');
    element.style.removeProperty('overflow');
    element.style.removeProperty('transition-duration');
    element.style.removeProperty('transition-property');
  }, duration);
}

/* resets element's style presets to 0 for slideUp/slideDown */
const hideEl = (element) => {
  element.style.height = 0;
  element.style.paddingTop = 0;
  element.style.paddingBottom = 0;
  element.style.marginTop = 0;
  element.style.marginBottom = 0;
  element.style.overflow = 'hidden';
}

/* toggles btw slideUp/slideDown depending on element's display settings */
const slideToggle = (elementId, duration)  => {
  var element = document.getElementById(elementId);
  if(window.getComputedStyle(element).display === 'none')
  {
    slideDown(elementId, duration);
  }
  else {
    slideUp(elementId, duration);
  }
}

/* fetches comments content from webserver and adds to DOM in container with elementID */
const addToDOM = (elementId) => {
  fetch("/comments").then(response => response.json()).then((comments) => {
    // add each parsed json.commentText to the DOM
    const destinationDiv = document.getElementById(elementId);
    for (comment in comments) {
      destinationDiv.appendChild(createSpan(comments[comment].commentText));
    }
  });
}

const createSpan = (text) => {
  const spanEl = document.createElement("span");
  spanEl.innerText = text;
  return spanEl;
}
