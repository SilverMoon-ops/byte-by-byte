// Primary CDN + official fallback host, per the project's own docs
const PRIMARY_URL =
  "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies";
const FALLBACK_URL = "https://latest.currency-api.pages.dev/v1/currencies";

const dropdowns = document.querySelectorAll(".dropdown select");
const btn = document.querySelector("form button");
const swapBtn = document.querySelector(".swap-btn");
const fromCurr = document.querySelector(".from select");
const toCurr = document.querySelector(".to select");
const amount = document.querySelector(".amount input");
const msg = document.querySelector(".msg");

let debounceTimer;

// Populate dropdowns from the currency list
for (const select of dropdowns) {
  for (const currCode in countryList) {
    const newOption = document.createElement("option");
    newOption.innerText = currCode;
    newOption.value = currCode;
    if (select.name === "from" && currCode === "USD") {
      newOption.selected = true;
    } else if (select.name === "to" && currCode === "INR") {
      newOption.selected = true;
    }
    select.append(newOption);
  }

  select.addEventListener("change", (evt) => {
    updateFlag(evt.target);
    updateExchangeRate();
  });
}

const updateFlag = (element) => {
  const currCode = element.value;
  const countryCode = countryList[currCode];
  const newSrc = `https://flagsapi.com/${countryCode}/flat/64.png`;
  const img = element.parentElement.querySelector("img");
  img.onerror = () => {
    img.onerror = null; // avoid infinite loop if the fallback also 404s
    img.src = "https://flagsapi.com/US/flat/64.png";
  };
  img.src = newSrc;
};

const updateExchangeRate = async () => {
  let amtVal = amount.value;
  if (amtVal === "" || Number(amtVal) < 1) {
    amtVal = 1;
    amount.value = "1";
  }

  msg.classList.add("loading");
  msg.innerText = "Fetching rate...";
  btn.disabled = true;

  const from = fromCurr.value.toLowerCase();
  const to = toCurr.value.toLowerCase();

  // The API returns ALL rates for a base currency in one response,
  // e.g. fetching "usd.json" gives { usd: { inr: 83.1, eur: 0.91, ... } }
  const fetchRates = async (base) => {
    const response = await fetch(`${base}/${from}.json`);
    if (!response.ok) throw new Error(`Request failed: ${response.status}`);
    return response.json();
  };

  try {
    let data;
    try {
      data = await fetchRates(PRIMARY_URL);
    } catch {
      data = await fetchRates(FALLBACK_URL); // jsDelivr down/slow, try the pages.dev host
    }

    const rate = data[from]?.[to];

    if (typeof rate !== "number") {
      throw new Error("Rate not available for this pair");
    }

    const finalAmount = (amtVal * rate).toFixed(2);
    msg.innerText = `${amtVal} ${fromCurr.value} = ${finalAmount} ${toCurr.value}`;
  } catch (err) {
    msg.innerText = "Couldn't fetch the rate. Please try again.";
    console.error(err);
  } finally {
    msg.classList.remove("loading");
    btn.disabled = false;
  }
};

const swapCurrencies = () => {
  const temp = fromCurr.value;
  fromCurr.value = toCurr.value;
  toCurr.value = temp;

  updateFlag(fromCurr);
  updateFlag(toCurr);
  updateExchangeRate();
};

btn.addEventListener("click", (evt) => {
  evt.preventDefault();
  updateExchangeRate();
});

swapBtn.addEventListener("click", (evt) => {
  evt.preventDefault();
  swapCurrencies();
});

// Debounced live update as the user types an amount
amount.addEventListener("input", () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(updateExchangeRate, 500);
});

window.addEventListener("load", () => {
  updateExchangeRate();
});
