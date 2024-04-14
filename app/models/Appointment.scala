package models

case class Appointment(
                        id: Option[Long] = None,
                        patientId: Long,
                        doctorId: Long,
                      )
