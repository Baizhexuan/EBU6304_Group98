"""Entry point for the TA recruitment system prototype.

This project intentionally uses only Python's standard library so the team can
run the prototype on lab machines without installing extra packages.
"""

from ui import RecruitmentApp


def main() -> None:
    """Start the desktop application."""
    app = RecruitmentApp()
    app.mainloop()


if __name__ == "__main__":
    main()
