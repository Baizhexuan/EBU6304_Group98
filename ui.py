"""Tkinter user interface for the TA recruitment prototype."""

from __future__ import annotations

import tkinter as tk
from tkinter import messagebox, ttk

from storage import Application, CsvRepository, Job, TAProfile


class RecruitmentApp(tk.Tk):
    """Desktop prototype covering the main recruitment workflow."""

    def __init__(self) -> None:
        super().__init__()
        self.title("TA Recruitment System - Group 98")
        self.geometry("1180x760")
        self.minsize(1000, 680)

        self.repository = CsvRepository()
        self._configure_styles()

        self.columnconfigure(0, weight=1)
        self.rowconfigure(0, weight=1)

        notebook = ttk.Notebook(self)
        notebook.grid(row=0, column=0, sticky="nsew", padx=12, pady=12)

        self.ta_tab = TATab(notebook, self.repository)
        self.mo_tab = MOTab(notebook, self.repository)

        notebook.add(self.ta_tab, text="TA Portal")
        notebook.add(self.mo_tab, text="MO Portal")

    def _configure_styles(self) -> None:
        """Use explicit colors so table rows remain visible across Windows themes."""
        style = ttk.Style(self)
        style.theme_use(style.theme_use())
        style.configure("Treeview", rowheight=26, foreground="#111111", background="#ffffff", fieldbackground="#ffffff")
        style.configure("Treeview.Heading", foreground="#111111", background="#f2f2f2")
        style.map("Treeview", background=[("selected", "#cfe8ff")], foreground=[("selected", "#111111")])


class TATab(ttk.Frame):
    """Student-facing functions: profile, browsing, applying, and status view."""

    def __init__(self, parent: ttk.Notebook, repository: CsvRepository) -> None:
        super().__init__(parent, padding=12)
        self.repository = repository
        self.selected_job_id: str | None = None

        self.columnconfigure(0, weight=2)
        self.columnconfigure(1, weight=3)
        self.rowconfigure(1, weight=1)

        self._build_profile_panel()
        self._build_job_browser()
        self._build_apply_panel()
        self._build_status_panel()
        self.refresh_jobs()
        self.refresh_statuses()

    def _build_profile_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="1. TA Profile Creation", padding=10)
        frame.grid(row=0, column=0, sticky="nsew", padx=(0, 8), pady=(0, 8))

        labels = [
            ("Student ID", "student_id"),
            ("Full Name", "full_name"),
            ("Email", "email"),
            ("Skills", "skills"),
            ("Availability", "availability"),
            ("CV File Path", "cv_file"),
        ]

        self.profile_entries: dict[str, ttk.Entry] = {}
        for row, (label, key) in enumerate(labels):
            ttk.Label(frame, text=label).grid(row=row, column=0, sticky="w", pady=4)
            entry = ttk.Entry(frame, width=40)
            entry.grid(row=row, column=1, sticky="ew", pady=4)
            self.profile_entries[key] = entry

        frame.columnconfigure(1, weight=1)
        ttk.Button(frame, text="Save / Update Profile", command=self.save_profile).grid(
            row=len(labels), column=0, columnspan=2, sticky="ew", pady=(8, 0)
        )

    def _build_job_browser(self) -> None:
        frame = ttk.LabelFrame(self, text="2. Browse Available Jobs", padding=10)
        frame.grid(row=1, column=0, sticky="nsew", padx=(0, 8), pady=(0, 8))
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(1, weight=1)

        self.job_count_var = tk.StringVar(value="Open jobs: loading...")
        ttk.Label(frame, textvariable=self.job_count_var).grid(row=0, column=0, columnspan=2, sticky="w", pady=(0, 8))

        self.job_listbox = tk.Listbox(
            frame,
            height=12,
            activestyle="dotbox",
            exportselection=False,
            font=("Segoe UI", 10),
            bg="#ffffff",
            fg="#111111",
            selectbackground="#cfe8ff",
            selectforeground="#111111",
        )
        self.job_listbox.grid(row=1, column=0, sticky="nsew")
        self.job_listbox.bind("<<ListboxSelect>>", self.show_job_details)

        scrollbar = ttk.Scrollbar(frame, orient="vertical", command=self.job_listbox.yview)
        scrollbar.grid(row=1, column=1, sticky="ns")
        self.job_listbox.configure(yscrollcommand=scrollbar.set)

        self.job_details = tk.Text(frame, height=7, wrap="word")
        self.job_details.grid(row=2, column=0, columnspan=2, sticky="ew", pady=(8, 0))
        self.job_details.configure(state="disabled")

        ttk.Button(frame, text="Refresh Jobs", command=self.refresh_jobs).grid(
            row=3, column=0, columnspan=2, sticky="ew", pady=(8, 0)
        )

    def _build_apply_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="3. Apply for a Job", padding=10)
        frame.grid(row=0, column=1, rowspan=2, sticky="nsew", pady=(0, 8))
        frame.columnconfigure(0, weight=1)

        ttk.Label(
            frame,
            text="Select a job from the table, then explain why you are suitable for it.",
            wraplength=420,
        ).grid(row=0, column=0, sticky="w")

        self.apply_target_var = tk.StringVar(value="Selected job: none")
        ttk.Label(frame, textvariable=self.apply_target_var).grid(row=1, column=0, sticky="w", pady=(8, 6))

        self.motivation_text = tk.Text(frame, height=10, wrap="word")
        self.motivation_text.grid(row=2, column=0, sticky="nsew")

        ttk.Button(frame, text="Submit Application", command=self.submit_application).grid(
            row=3, column=0, sticky="ew", pady=(8, 0)
        )

        ttk.Label(
            frame,
            text=(
                "Demo note: the application stores a snapshot of the TA profile so the MO can still "
                "review the details even if the student later updates their profile."
            ),
            wraplength=420,
            foreground="#444444",
        ).grid(row=4, column=0, sticky="w", pady=(10, 0))

        frame.rowconfigure(2, weight=1)

    def _build_status_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="4. Application Status", padding=10)
        frame.grid(row=2, column=0, columnspan=2, sticky="nsew")
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(0, weight=1)

        columns = ("application_id", "job_id", "ta_name", "status")
        self.status_tree = ttk.Treeview(frame, columns=columns, show="headings", height=8)
        for key, title in {
            "application_id": "Application ID",
            "job_id": "Job ID",
            "ta_name": "Applicant",
            "status": "Current Status",
        }.items():
            self.status_tree.heading(key, text=title)
            self.status_tree.column(key, width=160, anchor="center")
        self.status_tree.grid(row=0, column=0, sticky="nsew")

        scrollbar = ttk.Scrollbar(frame, orient="vertical", command=self.status_tree.yview)
        scrollbar.grid(row=0, column=1, sticky="ns")
        self.status_tree.configure(yscrollcommand=scrollbar.set)

        ttk.Button(frame, text="Refresh Status", command=self.refresh_statuses).grid(
            row=1, column=0, columnspan=2, sticky="ew", pady=(8, 0)
        )

    def save_profile(self) -> None:
        profile = TAProfile(
            student_id=self.profile_entries["student_id"].get().strip(),
            full_name=self.profile_entries["full_name"].get().strip(),
            email=self.profile_entries["email"].get().strip(),
            skills=self.profile_entries["skills"].get().strip(),
            availability=self.profile_entries["availability"].get().strip(),
            cv_file=self.profile_entries["cv_file"].get().strip(),
        )

        if not profile.student_id or not profile.full_name or not profile.email:
            messagebox.showwarning("Missing details", "Please fill in Student ID, Full Name, and Email.")
            return

        self.repository.upsert_profile(profile)
        self.refresh_statuses()
        messagebox.showinfo("Profile saved", "The TA profile has been saved successfully.")

    def refresh_jobs(self) -> None:
        self.job_listbox.delete(0, "end")

        jobs = self.repository.load_jobs()
        self.job_count_var.set(f"Open jobs: {len(jobs)}")

        for job in jobs:
            summary = (
                f"{job.job_id} | {job.module_code} | {job.module_name} | "
                f"{job.organiser_name} | {job.hours_per_week}h/week | {job.status}"
            )
            self.job_listbox.insert("end", summary)

        if not jobs:
            self.job_count_var.set("Open jobs: 0 (no jobs available yet)")
            self.job_details.configure(state="normal")
            self.job_details.delete("1.0", "end")
            self.job_details.insert("1.0", "No jobs are available. Use the MO Portal to publish a new job.")
            self.job_details.configure(state="disabled")
            self.selected_job_id = None
        else:
            self.job_listbox.selection_clear(0, "end")
            self.job_listbox.selection_set(0)
            self.show_job_details(None)

    def show_job_details(self, _event: object) -> None:
        selection = self.job_listbox.curselection()
        if not selection:
            return

        jobs = self.repository.load_jobs()
        selected_index = selection[0]
        if selected_index >= len(jobs):
            return

        job = jobs[selected_index]
        self.selected_job_id = job.job_id
        if job is None:
            return

        self.apply_target_var.set(f"Selected job: {job.job_id} - {job.module_name}")
        details = (
            f"Module: {job.module_name} ({job.module_code})\n"
            f"Organiser: {job.organiser_name}\n"
            f"Required skills: {job.required_skills}\n"
            f"Hours per week: {job.hours_per_week}\n"
            f"Status: {job.status}\n\n"
            f"Description:\n{job.description}"
        )
        self.job_details.configure(state="normal")
        self.job_details.delete("1.0", "end")
        self.job_details.insert("1.0", details)
        self.job_details.configure(state="disabled")

    def submit_application(self) -> None:
        if not self.selected_job_id:
            messagebox.showwarning("No job selected", "Please choose a job before applying.")
            return

        student_id = self.profile_entries["student_id"].get().strip()
        if not student_id:
            messagebox.showwarning("Profile required", "Please create or load a TA profile first.")
            return

        profiles = self.repository.load_profiles()
        profile = next((item for item in profiles if item.student_id == student_id), None)
        if profile is None:
            messagebox.showwarning("Profile not found", "Save your profile before submitting an application.")
            return

        existing = [
            application
            for application in self.repository.load_applications()
            if application.student_id == student_id and application.job_id == self.selected_job_id
        ]
        if existing:
            messagebox.showwarning("Duplicate application", "This TA has already applied for the selected job.")
            return

        application = Application(
            application_id=self.repository.next_application_id(),
            job_id=self.selected_job_id,
            student_id=profile.student_id,
            ta_name=profile.full_name,
            ta_email=profile.email,
            skills_snapshot=profile.skills,
            motivation=self.motivation_text.get("1.0", "end").strip(),
            status="Pending",
        )
        self.repository.add_application(application)
        self.motivation_text.delete("1.0", "end")
        self.refresh_statuses()
        messagebox.showinfo("Application submitted", "The application has been recorded with status Pending.")

    def refresh_statuses(self) -> None:
        for item in self.status_tree.get_children():
            self.status_tree.delete(item)

        student_id = self.profile_entries["student_id"].get().strip()
        applications = self.repository.load_applications()
        if student_id:
            applications = [application for application in applications if application.student_id == student_id]

        for application in applications:
            self.status_tree.insert(
                "",
                "end",
                values=(
                    application.application_id,
                    application.job_id,
                    application.ta_name,
                    application.status,
                ),
            )


class MOTab(ttk.Frame):
    """Module organiser functions: post jobs, review applicants, and select TAs."""

    def __init__(self, parent: ttk.Notebook, repository: CsvRepository) -> None:
        super().__init__(parent, padding=12)
        self.repository = repository

        self.columnconfigure(0, weight=2)
        self.columnconfigure(1, weight=3)
        self.rowconfigure(1, weight=1)

        self._build_post_job_panel()
        self._build_job_list_panel()
        self._build_application_panel()
        self.refresh_job_list()

    def _build_post_job_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="1. Post a Job", padding=10)
        frame.grid(row=0, column=0, sticky="nsew", padx=(0, 8), pady=(0, 8))

        labels = [
            ("Module Name", "module_name"),
            ("Module Code", "module_code"),
            ("Organiser Name", "organiser_name"),
            ("Required Skills", "required_skills"),
            ("Hours per Week", "hours_per_week"),
        ]
        self.job_entries: dict[str, ttk.Entry] = {}

        for row, (label, key) in enumerate(labels):
            ttk.Label(frame, text=label).grid(row=row, column=0, sticky="w", pady=4)
            entry = ttk.Entry(frame, width=40)
            entry.grid(row=row, column=1, sticky="ew", pady=4)
            self.job_entries[key] = entry

        ttk.Label(frame, text="Description").grid(row=len(labels), column=0, sticky="nw", pady=4)
        self.job_description_text = tk.Text(frame, height=5, wrap="word")
        self.job_description_text.grid(row=len(labels), column=1, sticky="ew", pady=4)

        ttk.Button(frame, text="Publish Job", command=self.publish_job).grid(
            row=len(labels) + 1, column=0, columnspan=2, sticky="ew", pady=(8, 0)
        )
        frame.columnconfigure(1, weight=1)

    def _build_job_list_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="2. Posted Jobs", padding=10)
        frame.grid(row=1, column=0, sticky="nsew", padx=(0, 8))
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(0, weight=1)

        columns = ("job_id", "module_name", "organiser_name", "status")
        self.mo_job_tree = ttk.Treeview(frame, columns=columns, show="headings", height=12)
        for key, title in {
            "job_id": "Job ID",
            "module_name": "Module",
            "organiser_name": "Organiser",
            "status": "Status",
        }.items():
            self.mo_job_tree.heading(key, text=title)
            self.mo_job_tree.column(key, width=140, anchor="center")
        self.mo_job_tree.column("module_name", width=220)
        self.mo_job_tree.grid(row=0, column=0, sticky="nsew")
        self.mo_job_tree.bind("<<TreeviewSelect>>", self.refresh_application_list)

        scrollbar = ttk.Scrollbar(frame, orient="vertical", command=self.mo_job_tree.yview)
        scrollbar.grid(row=0, column=1, sticky="ns")
        self.mo_job_tree.configure(yscrollcommand=scrollbar.set)

        ttk.Button(frame, text="Refresh Job List", command=self.refresh_job_list).grid(
            row=1, column=0, columnspan=2, sticky="ew", pady=(8, 0)
        )

    def _build_application_panel(self) -> None:
        frame = ttk.LabelFrame(self, text="3. Review Applicants", padding=10)
        frame.grid(row=0, column=1, rowspan=2, sticky="nsew")
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(1, weight=1)

        self.mo_selected_job_var = tk.StringVar(value="Selected job: none")
        ttk.Label(frame, textvariable=self.mo_selected_job_var).grid(row=0, column=0, sticky="w")

        columns = ("application_id", "ta_name", "ta_email", "skills_snapshot", "status")
        self.application_tree = ttk.Treeview(frame, columns=columns, show="headings", height=14)
        for key, title in {
            "application_id": "Application ID",
            "ta_name": "Applicant",
            "ta_email": "Email",
            "skills_snapshot": "Skills",
            "status": "Status",
        }.items():
            self.application_tree.heading(key, text=title)
            self.application_tree.column(key, width=140, anchor="center")
        self.application_tree.column("ta_email", width=180)
        self.application_tree.column("skills_snapshot", width=220)
        self.application_tree.grid(row=1, column=0, sticky="nsew", pady=(8, 0))

        button_bar = ttk.Frame(frame)
        button_bar.grid(row=2, column=0, sticky="ew", pady=(8, 0))
        button_bar.columnconfigure((0, 1, 2), weight=1)

        ttk.Button(button_bar, text="Mark Selected", command=lambda: self.update_selected_application("Selected")).grid(
            row=0, column=0, sticky="ew", padx=(0, 4)
        )
        ttk.Button(button_bar, text="Mark Rejected", command=lambda: self.update_selected_application("Rejected")).grid(
            row=0, column=1, sticky="ew", padx=4
        )
        ttk.Button(button_bar, text="Reset to Pending", command=lambda: self.update_selected_application("Pending")).grid(
            row=0, column=2, sticky="ew", padx=(4, 0)
        )

    def publish_job(self) -> None:
        values = {key: entry.get().strip() for key, entry in self.job_entries.items()}
        description = self.job_description_text.get("1.0", "end").strip()
        if not values["module_name"] or not values["module_code"] or not values["organiser_name"]:
            messagebox.showwarning("Missing details", "Please complete the module name, code, and organiser name.")
            return

        job = Job(
            job_id=self.repository.next_job_id(),
            module_name=values["module_name"],
            module_code=values["module_code"],
            organiser_name=values["organiser_name"],
            required_skills=values["required_skills"],
            hours_per_week=values["hours_per_week"] or "0",
            description=description,
            status="Open",
        )
        self.repository.add_job(job)

        for entry in self.job_entries.values():
            entry.delete(0, "end")
        self.job_description_text.delete("1.0", "end")
        self.refresh_job_list()
        messagebox.showinfo("Job posted", f"{job.job_id} has been published and is now visible to TAs.")

    def refresh_job_list(self) -> None:
        for item in self.mo_job_tree.get_children():
            self.mo_job_tree.delete(item)

        for job in self.repository.load_jobs():
            self.mo_job_tree.insert(
                "",
                "end",
                iid=job.job_id,
                values=(job.job_id, job.module_name, job.organiser_name, job.status),
            )
        self.refresh_application_list()

    def refresh_application_list(self, _event: object | None = None) -> None:
        for item in self.application_tree.get_children():
            self.application_tree.delete(item)

        selection = self.mo_job_tree.selection()
        if not selection:
            self.mo_selected_job_var.set("Selected job: none")
            return

        job_id = selection[0]
        self.mo_selected_job_var.set(f"Selected job: {job_id}")
        applications = [
            application for application in self.repository.load_applications() if application.job_id == job_id
        ]
        for application in applications:
            self.application_tree.insert(
                "",
                "end",
                iid=application.application_id,
                values=(
                    application.application_id,
                    application.ta_name,
                    application.ta_email,
                    application.skills_snapshot,
                    application.status,
                ),
            )

    def update_selected_application(self, new_status: str) -> None:
        selection = self.application_tree.selection()
        if not selection:
            messagebox.showwarning("No application selected", "Please choose an application to update.")
            return

        application_id = selection[0]
        self.repository.update_application_status(application_id, new_status)
        self.refresh_application_list()
        messagebox.showinfo("Status updated", f"The application status is now {new_status}.")
