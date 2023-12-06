const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  service: "Gmail",
  auth: {
    user: "juanfranciscoleal13@gmail.com",
    pass: "Hefita13",
  },
});

exports.sendEmailOnButton1Click = functions.https.onRequest((req, res) => {
  const mailOptions = {
    from: "juan@francisco.com",
    to: "juanfranciscoleal13@gmail.com",
    subject: "Test",
    text: "Este es el cuerpo del correo electrónico.",
  };

  transporter.sendMail(mailOptions, (error, info) => {
    if (error) {
      console.error("Error al enviar el correo electrónico:", error);
      res.status(500).send("Error al enviar el correo electrónico");
    } else {
      console.log("Correo electrónico enviado:", info.response);
      res.status(200).send("Correo electrónico enviado con éxito");
    }
  });
});
