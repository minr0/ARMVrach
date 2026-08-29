from django.db import models
from django.contrib.auth.models import User


class PatientProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='patient_profile')
    iin = models.CharField(max_length=12, unique=True, verbose_name='ИИН')
    phone = models.CharField(max_length=20, blank=True, verbose_name='Телефон')
    address = models.CharField(max_length=255, blank=True, verbose_name='Адрес (село/участок)')
    date_of_birth = models.DateField(null=True, blank=True, verbose_name='Дата рождения')

    class Meta:
        verbose_name = 'Профиль пациента'
        verbose_name_plural = 'Профили пациентов'

    def __str__(self):
        return f'{self.user.get_full_name()} (ИИН: {self.iin})'


class Appointment(models.Model):
    STATUS_CHOICES = [
        ('waiting', 'Ожидание'),
        ('accepted', 'Принят'),
        ('done', 'Завершён'),
        ('cancelled', 'Отменён'),
    ]

    patient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='appointments', verbose_name='Пациент')
    datetime = models.DateTimeField(verbose_name='Дата и время')
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='waiting', verbose_name='Статус')
    complaint = models.TextField(verbose_name='Жалоба')
    doctor_note = models.TextField(blank=True, verbose_name='Заметка врача')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Запись на приём'
        verbose_name_plural = 'Записи на приём'
        ordering = ['datetime']

    def __str__(self):
        return f'{self.patient.get_full_name()} — {self.datetime.strftime("%d.%m.%Y %H:%M")}'


class TestResult(models.Model):
    patient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='test_results', verbose_name='Пациент')
    indicator = models.CharField(max_length=100, verbose_name='Показатель (глюкоза, давление и т.д.)')
    value = models.CharField(max_length=50, verbose_name='Значение')
    unit = models.CharField(max_length=30, blank=True, verbose_name='Единица измерения')
    norm = models.CharField(max_length=50, blank=True, verbose_name='Норма')
    date = models.DateField(verbose_name='Дата')
    added_by = models.ForeignKey(
        User, on_delete=models.SET_NULL, null=True, related_name='added_results', verbose_name='Добавил'
    )

    class Meta:
        verbose_name = 'Результат анализа'
        verbose_name_plural = 'Результаты анализов'
        ordering = ['-date']

    def __str__(self):
        return f'{self.patient.get_full_name()} — {self.indicator}: {self.value} {self.unit}'


class HealthLog(models.Model):
    patient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='health_logs', verbose_name='Пациент')
    date = models.DateField(verbose_name='Дата')
    weight = models.DecimalField(max_digits=5, decimal_places=1, null=True, blank=True, verbose_name='Вес (кг)')
    systolic = models.IntegerField(null=True, blank=True, verbose_name='Давление систолическое')
    diastolic = models.IntegerField(null=True, blank=True, verbose_name='Давление диастолическое')
    blood_sugar = models.DecimalField(max_digits=4, decimal_places=1, null=True, blank=True, verbose_name='Сахар крови (ммоль/л)')
    note = models.TextField(blank=True, verbose_name='Заметка')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Запись дневника здоровья'
        verbose_name_plural = 'Дневник здоровья'
        ordering = ['-date']

    def __str__(self):
        return f'{self.patient.get_full_name()} — {self.date}'


class ChatMessage(models.Model):
    patient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='chat_messages', verbose_name='Пациент')
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sent_messages', verbose_name='Отправитель')
    text = models.TextField(verbose_name='Текст сообщения')
    created_at = models.DateTimeField(auto_now_add=True)
    is_read = models.BooleanField(default=False, verbose_name='Прочитано')

    class Meta:
        verbose_name = 'Сообщение'
        verbose_name_plural = 'Сообщения'
        ordering = ['created_at']

    def __str__(self):
        return f'[{self.created_at.strftime("%d.%m %H:%M")}] {self.sender.get_full_name()}: {self.text[:40]}'
