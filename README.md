# Doctor's Workspace & Patient Mobile App

A comprehensive system designed to automate a doctor's daily workflow and provide remote patient monitoring, powered by an integrated AI assistant using the **Google Gemini API**.

The project consists of a web dashboard (Doctor's Workspace) built with **Django REST Framework** and a mobile client application for patients.

---

## Key Features

### Web Interface (Doctor's Workspace)
* **Dashboard:** Real-time statistics (today's appointments, pending requests, unread messages) and appointment schedules.
* **Patient Registry:** Searchable patient list with detailed profiles (national ID/IIN, contact info, address).
* **Electronic Health Record (EHR):**
  * **Appointments:** Visit history and active booking requests.
  * **Lab Results:** View diagnostic reports and enter new blood test results.
  * **Health Log:** Interactive charts tracking weight, blood pressure (systolic/diastolic), and blood glucose trends.
  * **Chat:** Direct messaging with the patient.
* **Global Chat Hub:** A unified inbox managing all active patient conversations in one place.

### Mobile Application (Patient)
* **Authentication:** Secure login via National ID (IIN) and password, connecting automatically to the server domain.
* **Lab Reports:** Access diagnostic test results directly synchronized from the clinic's database.
* **Appointment Booking:** Submit appointment requests by selecting dates and specifying health concerns.
* **Chat & AI Assistant:** Direct communication with the attending doctor, plus 24/7 AI health support triggered by keywords like `AI` or `Bot`.

---

## Tech Stack

* **Backend:** Python, Django, Django REST Framework (DRF)
* **Database:** SQLite / PostgreSQL
* **AI Integration:** Google Gemini API (`google-generativeai` / `google-genai`)
* **Hosting / Infrastructure:** PythonAnywhere
