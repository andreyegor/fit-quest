import dropmenuToggle from "../scripts/dropmenuToggle.ts";
import Trainings from "./scripts/Trainings.ts";
import LogoutButton from "../scripts/LogoutButton.ts";
import ApiRequests from "../scripts/ApiRequests.ts";
import CalendarButton from "../scripts/CalendarButton.ts";
import TimePeriodStore from "../scripts/TimePeriodStore.ts";

const userNameFieldElement = document.querySelector(".user-button__name")
ApiRequests.getUserInfo()
if (userNameFieldElement) {
    userNameFieldElement.textContent = localStorage.getItem("username")
}

dropmenuToggle()
new LogoutButton()

const timePeriodStore = new TimePeriodStore([
    new Date(new Date().getFullYear(), new Date().getMonth(), 1),
    new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0, 23, 59, 59, 999)
])
new Trainings(timePeriodStore)
new CalendarButton(timePeriodStore)

