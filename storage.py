"""CSV-based storage helpers for the TA recruitment prototype.

The course brief forbids a database, so this module centralises all file access
using human-readable CSV files. Keeping the logic here also makes it easier to
replace CSV with JSON later if the team changes direction in future iterations.
"""

from __future__ import annotations

import csv
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Iterable


BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"


@dataclass(slots=True)
class TAProfile:
    student_id: str
    full_name: str
    email: str
    skills: str
    availability: str
    cv_file: str


@dataclass(slots=True)
class Job:
    job_id: str
    module_name: str
    module_code: str
    organiser_name: str
    required_skills: str
    hours_per_week: str
    description: str
    status: str


@dataclass(slots=True)
class Application:
    application_id: str
    job_id: str
    student_id: str
    ta_name: str
    ta_email: str
    skills_snapshot: str
    motivation: str
    status: str


class CsvRepository:
    """Simple repository that persists system data into CSV files."""

    def __init__(self) -> None:
        self._ensure_storage()

    def _ensure_storage(self) -> None:
        """Create the data folder and starter CSV files when missing."""
        DATA_DIR.mkdir(parents=True, exist_ok=True)
        self._ensure_file(DATA_DIR / "profiles.csv", TAProfile.__annotations__.keys())
        self._ensure_file(DATA_DIR / "jobs.csv", Job.__annotations__.keys())
        self._ensure_file(DATA_DIR / "applications.csv", Application.__annotations__.keys())
        self._seed_jobs()

    @staticmethod
    def _ensure_file(path: Path, fieldnames: Iterable[str]) -> None:
        if path.exists():
            return
        with path.open("w", newline="", encoding="utf-8") as csv_file:
            writer = csv.DictWriter(csv_file, fieldnames=fieldnames)
            writer.writeheader()

    def _seed_jobs(self) -> None:
        """Insert a few jobs so the demo is usable immediately."""
        jobs = self.load_jobs()
        if jobs:
            return
        starter_jobs = [
            Job(
                job_id="J001",
                module_name="Software Engineering",
                module_code="EBU6304",
                organiser_name="Dr Chen",
                required_skills="Python, teamwork, communication",
                hours_per_week="6",
                description="Support workshops, answer student questions, and mark practice tasks.",
                status="Open",
            ),
            Job(
                job_id="J002",
                module_name="Database Systems",
                module_code="EBU6201",
                organiser_name="Dr Wang",
                required_skills="SQL, data modelling, Excel",
                hours_per_week="4",
                description="Assist with lab sessions and help maintain teaching materials.",
                status="Open",
            ),
        ]
        self.save_jobs(starter_jobs)

    @staticmethod
    def _read_rows(path: Path) -> list[dict[str, str]]:
        with path.open("r", newline="", encoding="utf-8") as csv_file:
            return list(csv.DictReader(csv_file))

    @staticmethod
    def _write_rows(path: Path, rows: Iterable[dict[str, str]], fieldnames: Iterable[str]) -> None:
        with path.open("w", newline="", encoding="utf-8") as csv_file:
            writer = csv.DictWriter(csv_file, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(rows)

    def load_profiles(self) -> list[TAProfile]:
        rows = self._read_rows(DATA_DIR / "profiles.csv")
        return [TAProfile(**row) for row in rows]

    def load_jobs(self) -> list[Job]:
        rows = self._read_rows(DATA_DIR / "jobs.csv")
        return [Job(**row) for row in rows]

    def load_applications(self) -> list[Application]:
        rows = self._read_rows(DATA_DIR / "applications.csv")
        return [Application(**row) for row in rows]

    def save_profiles(self, profiles: Iterable[TAProfile]) -> None:
        self._write_rows(
            DATA_DIR / "profiles.csv",
            (asdict(profile) for profile in profiles),
            TAProfile.__annotations__.keys(),
        )

    def save_jobs(self, jobs: Iterable[Job]) -> None:
        self._write_rows(
            DATA_DIR / "jobs.csv",
            (asdict(job) for job in jobs),
            Job.__annotations__.keys(),
        )

    def save_applications(self, applications: Iterable[Application]) -> None:
        self._write_rows(
            DATA_DIR / "applications.csv",
            (asdict(application) for application in applications),
            Application.__annotations__.keys(),
        )

    def upsert_profile(self, profile: TAProfile) -> None:
        profiles = self.load_profiles()
        for index, existing in enumerate(profiles):
            if existing.student_id == profile.student_id:
                profiles[index] = profile
                self.save_profiles(profiles)
                return
        profiles.append(profile)
        self.save_profiles(profiles)

    def add_job(self, job: Job) -> None:
        jobs = self.load_jobs()
        jobs.append(job)
        self.save_jobs(jobs)

    def add_application(self, application: Application) -> None:
        applications = self.load_applications()
        applications.append(application)
        self.save_applications(applications)

    def update_application_status(self, application_id: str, new_status: str) -> None:
        applications = self.load_applications()
        for application in applications:
            if application.application_id == application_id:
                application.status = new_status
                break
        self.save_applications(applications)

    def next_job_id(self) -> str:
        jobs = self.load_jobs()
        return f"J{len(jobs) + 1:03d}"

    def next_application_id(self) -> str:
        applications = self.load_applications()
        return f"A{len(applications) + 1:03d}"
