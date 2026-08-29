"""
Management command: python manage.py create_demo_data

Creates:
  - 1 doctor user (username: doctor, password: doctor123)
  - 2 patient users with profiles, appointments, test results, health logs
"""
from django.core.management.base import BaseCommand
from django.contrib.auth.models import User
from django.utils import timezone
from datetime import date, timedelta
import random


class Command(BaseCommand):
    help = 'Creates demo data for development'

    def handle(self, *args, **kwargs):
        from arm_doctor.models import PatientProfile, Appointment, TestResult, HealthLog, ChatMessage

        # Doctor
        doctor, _ = User.objects.get_or_create(username='doctor', defaults={
            'first_name': 'Айгуль', 'last_name': 'Жаксыбекова',
            'is_staff': True, 'is_superuser': True,
        })
        doctor.set_password('doctor123')
        doctor.save()
        self.stdout.write(f'  Doctor: doctor / doctor123')

        # Patients
        patients_data = [
            ('patient1', 'Асет', 'Нурмаганбетов', '870123456789', '+77771112233', 'с. Коктал, уч. 5'),
            ('patient2', 'Жанар', 'Сейткали', '920987654321', '+77779998877', 'с. Аксу, уч. 2'),
        ]

        for username, first, last, iin, phone, address in patients_data:
            user, _ = User.objects.get_or_create(username=username, defaults={
                'first_name': first, 'last_name': last, 'is_staff': False
            })
            user.set_password('patient123')
            user.save()

            PatientProfile.objects.get_or_create(user=user, defaults={
                'iin': iin, 'phone': phone, 'address': address,
                'date_of_birth': date(1990, 3, 15),
            })

            # Appointments
            for i in range(3):
                dt = timezone.now() + timedelta(hours=i - 1)
                Appointment.objects.get_or_create(
                    patient=user, datetime=dt,
                    defaults={
                        'status': random.choice(['waiting', 'accepted']),
                        'complaint': random.choice([
                            'Головная боль, слабость',
                            'Боль в суставах',
                            'Повышенное давление, головокружение',
                        ])
                    }
                )

            # Test results
            indicators = [
                ('Глюкоза', '5.4', 'ммоль/л', '3.9–6.1'),
                ('Гемоглобин', '132', 'г/л', '120–160'),
                ('Давление', '130/85', 'мм рт.ст.', '120/80'),
                ('Холестерин', '4.8', 'ммоль/л', '<5.2'),
            ]
            for ind, val, unit, norm in indicators:
                TestResult.objects.get_or_create(
                    patient=user, indicator=ind,
                    defaults={'value': val, 'unit': unit, 'norm': norm,
                              'date': date.today() - timedelta(days=7), 'added_by': doctor}
                )

            # Health logs
            for i in range(14):
                d = date.today() - timedelta(days=13 - i)
                HealthLog.objects.get_or_create(patient=user, date=d, defaults={
                    'weight': round(random.uniform(68, 72), 1),
                    'systolic': random.randint(118, 140),
                    'diastolic': random.randint(75, 90),
                    'blood_sugar': round(random.uniform(4.8, 6.5), 1),
                })

            # Chat
            ChatMessage.objects.get_or_create(
                patient=user, sender=user,
                defaults={'text': 'Здравствуйте, доктор. Когда можно прийти на повторный осмотр?'}
            )

            self.stdout.write(f'  Patient: {username} / patient123  ({first} {last})')

        self.stdout.write(self.style.SUCCESS('\nДемо-данные созданы успешно!'))
