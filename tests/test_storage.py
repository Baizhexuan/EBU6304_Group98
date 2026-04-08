"""Basic smoke tests for the CSV repository."""

from __future__ import annotations

import shutil
import unittest
from pathlib import Path
from unittest.mock import patch

import storage
from storage import Application, CsvRepository, Job, TAProfile


class CsvRepositoryTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp_dir = Path(__file__).resolve().parents[1] / ".test_tmp"
        shutil.rmtree(self.temp_dir, ignore_errors=True)
        self.temp_dir.mkdir(parents=True, exist_ok=True)
        self.addCleanup(lambda: shutil.rmtree(self.temp_dir, ignore_errors=True))

        self.data_dir = self.temp_dir / "data"
        self.base_dir = self.temp_dir

        self.base_patch = patch.object(storage, "BASE_DIR", self.base_dir)
        self.data_patch = patch.object(storage, "DATA_DIR", self.data_dir)
        self.base_patch.start()
        self.data_patch.start()
        self.addCleanup(self.base_patch.stop)
        self.addCleanup(self.data_patch.stop)

        self.repository = CsvRepository()

    def test_upsert_profile_and_load(self) -> None:
        profile = TAProfile(
            student_id="S001",
            full_name="Alice Example",
            email="alice@example.com",
            skills="Python, SQL",
            availability="Mon/Wed",
            cv_file="alice_cv.pdf",
        )
        self.repository.upsert_profile(profile)

        profiles = self.repository.load_profiles()
        self.assertEqual(len(profiles), 1)
        self.assertEqual(profiles[0].full_name, "Alice Example")

    def test_add_job_and_application(self) -> None:
        job = Job(
            job_id=self.repository.next_job_id(),
            module_name="Web Development",
            module_code="EBU6001",
            organiser_name="Dr Lin",
            required_skills="HTML, CSS, JavaScript",
            hours_per_week="5",
            description="Support labs.",
            status="Open",
        )
        self.repository.add_job(job)

        application = Application(
            application_id=self.repository.next_application_id(),
            job_id=job.job_id,
            student_id="S001",
            ta_name="Alice Example",
            ta_email="alice@example.com",
            skills_snapshot="HTML, CSS",
            motivation="I have helped teach frontend labs before.",
            status="Pending",
        )
        self.repository.add_application(application)
        self.repository.update_application_status(application.application_id, "Selected")

        applications = self.repository.load_applications()
        self.assertEqual(len(applications), 1)
        self.assertEqual(applications[0].status, "Selected")


if __name__ == "__main__":
    unittest.main()
