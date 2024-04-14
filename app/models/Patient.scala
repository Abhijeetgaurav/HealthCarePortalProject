package models

case class Patient(
                    id: Option[Long] = None,
                    name: String,
                    age: Int,
                    disease: String,
                    location: String,
                    email: String,
                    contactNumber: String
                  )
