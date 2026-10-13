
const from = document.getElementById("from");
const to = document.getElementById("to");
const search = document.getElementById("search");
const date = document.getElementById("date");
const available = document.getElementById("available-buses");
const busesBox = document.getElementById("buses");
const finding = document.getElementById("finding");
const seatsContainer = document.getElementById("seats-container");
const seatCount = document.getElementById("SelectedSeats");
const show = document.getElementById("show");
const passengerBox = document.getElementById("passenger");
const bookingSummary = document.getElementById("bookingSummary");
const paymentBox = document.querySelector(".payment");
const method = document.getElementById("method");
const pay = document.getElementById("pay");
const payNow = document.getElementById("payNow");
let seats = document.querySelectorAll(".seats");

// selected Bus and seats are stored here
let SelectedBus;
let SelectedSeats = [];
let passengers = [];
let buses = [];
//creating restrictions for the date inputs

// const today = new Date();
// date.min = today.toISOString().split("T")[0];
// const maxDate = new Date(today);
// maxDate.setMonth(maxDate.getMonth() + 3);
// date.max = maxDate.toISOString().split("T")[0];


search.onclick = async function () {
    available.innerHTML = "";

    if (!from.value || !to.value || !date.value) {
        available.textContent = "Please select source, destination and date.";
        busesBox.style.display = "block";
        return;
    }

    const params = new URLSearchParams({
        from: from.value,
        to: to.value,
        date: date.value
    });

    try {
        const response = await fetch(
            `http://localhost:8080/api/buses?${params}`
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Failed to fetch buses");
        }

        buses = data;

        if (buses.length === 0) {
            available.textContent = "No buses found!";
        }

        buses.forEach(bus => {
            const busElement = document.createElement("p");

            busElement.textContent =
                `${bus.name} | ${bus.from} → ${bus.to} | ${bus.date} | ₹${bus.fare} | ${bus.availableSeats} seats `;

            const button = document.createElement("button");
            button.textContent = "Book Now";

            button.addEventListener("click", () => {
                bookNow(bus);
            });

            busElement.appendChild(button);
            available.appendChild(busElement);
        });

    } catch (error) {
        console.error("Error fetching buses:", error);
        available.textContent = "Unable to load buses. Please try again.";
    }

    busesBox.style.display = "block";
};

async function loadCities() {
    try {
        const response = await fetch("http://localhost:8080/api/cities");

        if (!response.ok) {
            throw new Error("Failed to load cities");
        }

        const data = await response.json();
        const cities = data.cities;

        [from, to].forEach((dropdown, index) => {
            const placeholder = index === 0
                ? "Select source"
                : "Select destination";

            dropdown.innerHTML = "";

            const option = document.createElement("option");
            option.value = "";
            option.textContent = placeholder;
            dropdown.appendChild(option);

            cities.forEach(city => {
                const option = document.createElement("option");
                option.value = city;
                option.textContent = city;
                dropdown.appendChild(option);
            });
        });

    } catch (error) {
        console.error("Error loading cities:", error);
    }
}

loadCities();

function bookNow(bus) {
    SelectedBus = bus;
    finding.style.display = "none";
    busesBox.style.display = "none";
    loadBookedSeats(bus.id);
    seatsContainer.style.display = "block";
}

async function loadBookedSeats(busId) {
    try {
        const response = await fetch(
            `http://localhost:8080/api/booked-seats?busId=${busId}`
        );

        if (!response.ok) {
            throw new Error("Could not load booked seats");
        }

        const data = await response.json();
        const bookedSeats = data.bookedSeats;

        document.querySelectorAll(".seats").forEach(seat => {
            const seatNumber = Number(seat.textContent.trim());

            if (bookedSeats.includes(seatNumber)) {
                seat.classList.add("booked");
                seat.disabled = true;
            } else {
                seat.classList.remove("booked");
                seat.disabled = false;
            }
        });

    } catch (error) {
        console.error("Error loading booked seats:", error);
    }
}

//Seat Selection
seats.forEach(element => {

    element.addEventListener("click", event => {
        if (event.target.disabled) return;
        //Checking if seat is already selected
        if (event.target.classList.contains("selected")) {
            event.target.classList.remove("selected");
            SelectedSeats.splice(SelectedSeats.indexOf(event.target.textContent), 1);
            seatCount.textContent = SelectedSeats.length;
            show.textContent = SelectedSeats.join(", ");
        }
        else {
            if (SelectedSeats.length < 5) {
                event.target.classList.add("selected");
                SelectedSeats.push(event.target.textContent);

                seatCount.textContent = SelectedSeats.length;
                show.textContent = SelectedSeats.join(", ");

            } else {
                show.textContent = " Can't Select More than 5 Seats";
            }
        }

    });
});


// validation of form
function validatePassenger(form) {
    const name = form.elements["name"].value.trim();
    const age = Number(form.elements["age"].value);
    const phone = form.elements["phone"].value.trim();
    const email = form.elements["email"].value.trim();

    if (!/^[A-Za-z ]+$/.test(name)) {
        return "Name must contain only letters";
    }
    if (age <= 18) {
        return "Age must be above 18";
    }
    if (!email.includes("@")) {
        return "Email must contain @";
    }
    if (!/^[0-9]{10}$/.test(phone)) {
        return "Phone number must be exactly 10 digits";
    }
    return "";
}


pay.addEventListener("click", event => {
    if (SelectedSeats.length === 0) {
        show.textContent = "No Seat Selected";
    }
    else {
        passengerBox.style.display = "block";

        // passenger form created
        SelectedSeats.forEach((seat, index) => {
            let form = document.createElement("form");
            form.setAttribute("data-seat", seat);
            form.innerHTML = `
               <h3>Passenger ${index + 1}</h3>
               <p>Seat: ${seat}</p>
               
               <input type="text" name="name" placeholder="Name">
               <input type="number" name="age" placeholder="Age">
               <input type="tel" name="phone" placeholder="Phone">
               <input type="email" name="email" placeholder="Email">
               <input type="file" name="proof" accept="image/png, image/jpeg">       
         `;
            passengerBox.appendChild(form);
            seatsContainer.style.display = "none";
        });

        let continueBtn = document.createElement("button");
        continueBtn.type = "button";
        continueBtn.textContent = "Continue to Payment";
        passengerBox.appendChild(continueBtn);

        continueBtn.addEventListener("click", event => {
            passengers = [];
            const forms = document.querySelectorAll("#passenger form");
            let valid = true;

            forms.forEach(form => {
                if (!valid) return;

                if (
                    form.elements["name"].value === "" ||
                    form.elements["age"].value === "" ||
                    form.elements["phone"].value === "" ||
                    form.elements["email"].value === "" ||
                    !form.elements["proof"].files[0]
                ) {
                    alert("Seat " + form.dataset.seat + ": Please fill all the fields");
                    valid = false;
                    return;
                }

                const error = validatePassenger(form);
                if (error !== "") {
                    alert("Seat " + form.dataset.seat + ": " + error);
                    valid = false;
                    return;
                }

                let passenger = {
                    seat: form.dataset.seat,
                    name: form.elements["name"].value,
                    age: form.elements["age"].value,
                    phone: form.elements["phone"].value,
                    email: form.elements["email"].value,
                    proof: form.elements["proof"].files[0]
                };
                passengers.push(passenger);
            });

            if (!valid) return;

            bookingSummary.innerHTML = `
                <h3>Booking Summary:</h3>
                
                <p><strong>Bus:</strong>${SelectedBus.name}</p>
                <p><strong>Route:</strong>${SelectedBus.from} -> ${SelectedBus.to}</p>
                <p><strong>Date:</strong>${SelectedBus.date}</p>
                <p><strong>Selected Seats:</strong>${SelectedSeats.join(", ")}</p>
                
                <h3>Passenger Details</h3>
            `;

            passengers.forEach(element => {
                bookingSummary.innerHTML += `
        <p>
            Seat: ${element.seat} |
            Name: ${element.name} |
            Age: ${element.age}
        </p>
    `;
            });

            passengerBox.style.display = "none";
            paymentBox.style.display = "block";
        });
    }
});

payNow.addEventListener("click", async () => {
    const selectedMethod = document.querySelector(`input[name="paymentMethod"]:checked`);

    if (!selectedMethod) {
        method.textContent = "Please choose a Payment Method";
        return;
    }
    try {
        for (const passenger of passengers) {
            const response = await fetch("http://localhost:8080/api/booking", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    busId: SelectedBus.id,
                    seatNumber: passenger.seat,
                    name: passenger.name,
                    age: passenger.age,
                    email: passenger.email,
                    phone: passenger.phone
                }).toString()
            });
            if (!response.ok) {
                throw new Error("Booking failed");
            }
            const result = await response.json();
            console.log(result.message, result.bookingId);
        }
        console.log("All bookings saved successfully");
    }catch (error) {
        console.error("Booking failed:", error.message);
        method.textContent = error.message;
        return;
    }
    paymentBox.style.display = "none";

    document.body.innerHTML += `
<div id="booking-success">
            <h3>Booking Successful:</h3>

                <p><strong>Bus:</strong>${SelectedBus.name}</p>
                <p><strong>Route:</strong>${SelectedBus.from} -> ${SelectedBus.to}</p>
                <p><strong>Date:</strong>${SelectedBus.date}</p>
                <p><strong>Selected Seats:</strong>${SelectedSeats.join(", ")}</p>
                <p><strong>Payment Method:</strong> ${selectedMethod.value}</p>

  </div>
`;
});
