from rest_framework import serializers
from django.contrib.auth.models import User
from .models import PatientProfile, Appointment, TestResult, HealthLog, ChatMessage


class PatientProfileSerializer(serializers.ModelSerializer):
    full_name = serializers.SerializerMethodField()

    class Meta:
        model = PatientProfile
        fields = ['id', 'iin', 'phone', 'address', 'date_of_birth', 'full_name']

    def get_full_name(self, obj):
        return obj.user.get_full_name()


class AppointmentSerializer(serializers.ModelSerializer):
    patient_name = serializers.SerializerMethodField()

    class Meta:
        model = Appointment
        fields = ['id', 'patient', 'patient_name', 'datetime', 'status', 'complaint', 'doctor_note', 'created_at']
        read_only_fields = ['patient', 'doctor_note', 'created_at']

    def get_patient_name(self, obj):
        return obj.patient.get_full_name()

    def create(self, validated_data):
        # Automatically assign the requesting user as patient
        validated_data['patient'] = self.context['request'].user
        return super().create(validated_data)


class TestResultSerializer(serializers.ModelSerializer):
    class Meta:
        model = TestResult
        fields = ['id', 'patient', 'indicator', 'value', 'unit', 'norm', 'date', 'added_by']
        read_only_fields = ['patient', 'added_by']


class HealthLogSerializer(serializers.ModelSerializer):
    class Meta:
        model = HealthLog
        fields = ['id', 'patient', 'date', 'weight', 'systolic', 'diastolic', 'blood_sugar', 'note', 'created_at']
        read_only_fields = ['patient', 'created_at']

    def create(self, validated_data):
        validated_data['patient'] = self.context['request'].user
        return super().create(validated_data)


class ChatMessageSerializer(serializers.ModelSerializer):
    sender_name = serializers.SerializerMethodField()
    is_from_doctor = serializers.SerializerMethodField()

    class Meta:
        model = ChatMessage
        fields = ['id', 'patient', 'sender', 'sender_name', 'text', 'created_at', 'is_read', 'is_from_doctor']
        read_only_fields = ['patient', 'sender', 'created_at', 'is_read']

    def get_sender_name(self, obj):
        return obj.sender.get_full_name() or obj.sender.username

    def get_is_from_doctor(self, obj):
        return obj.sender.is_staff

    def create(self, validated_data):
        request = self.context['request']
        validated_data['sender'] = request.user
        validated_data['patient'] = request.user  # patient sends to doctor
        return super().create(validated_data)


# Serializer for auth (login by IIN)
class IINLoginSerializer(serializers.Serializer):
    iin = serializers.CharField(max_length=12, min_length=12)
    password = serializers.CharField()
