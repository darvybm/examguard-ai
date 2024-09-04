from flask import Flask, request, jsonify, abort
from cheat_detector_optimized import CheatDetector
import os
import jwt
import base64

app = Flask(__name__)
detector = CheatDetector('model/best.pt')

SECRET_KEY_ENV = os.environ.get('JWT_SECRET_KEY')
if not SECRET_KEY_ENV:
    SECRET_KEY_ENV = "add-key-here"

print(SECRET_KEY_ENV)

SECRET_KEY = base64.b64decode(SECRET_KEY_ENV)

@app.before_request
def verify_jwt():
    auth_header = request.headers.get('Authorization')
    print(f"Authorization header: {auth_header}")
    if not auth_header:
        abort(403)  # Forbidden

    try:
        token = auth_header.split(" ")[1]  # Extrae el token
        print(f"Token recibido: {token}")

        decoded = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        print(f"Token decodificado: {decoded}")
        print(f"Token válido para: {decoded['sub']}")
        print(f"Token expira: {decoded['exp']}")  # Imprime la fecha de expiración

    except jwt.ExpiredSignatureError:
        print("El token ha expirado")
        abort(403, "El token ha expirado")
    except jwt.InvalidTokenError:
        print("Token inválido")
        abort(403, "Token inválido")
    except IndexError:
        print("Formato de token incorrecto")
        abort(403, "Formato de token incorrecto")


def create_folder_structure(base_path):
    if not os.path.exists(base_path):
        os.makedirs(base_path)


@app.route('/api/store', methods=['POST'])
def store_recording():
    try:
        # Obtener los datos del cuerpo de la solicitud
        user_id = request.form.get('userId')
        folder_id = request.form.get('folderId')
        recording_id = request.form.get('recordingId')
        file = request.files['file']

        # Definir la estructura de carpetas con nombres descriptivos
        user_folder = f'user_{user_id}'
        folder_folder = f'folder_{folder_id}'
        recording_folder = f'recording_{recording_id}'
        folder_path = os.path.join('recordings', user_folder, folder_folder, recording_folder)
        print(folder_path)

        # Crear la estructura de carpetas
        create_folder_structure(folder_path)

        # Nombre de archivo para almacenar
        unprocessed_file_path = os.path.join(folder_path, f'{recording_id}.mp4')

        # Guardar el archivo localmente
        file.save(unprocessed_file_path)

        return jsonify({'message': 'File stored successfully', 'compressed_file': unprocessed_file_path}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/process', methods=['POST'])
def process_recording():
    try:
        data = request.get_json()
        user_id = data.get('userId')
        folder_id = data.get('folderId')
        recording_id = data.get('recordingId')

        # Definir la ruta del archivo sin procesar con nombres descriptivos
        user_folder = f'user_{user_id}'
        folder_folder = f'folder_{folder_id}'
        recording_folder = f'recording_{recording_id}'
        unprocessed_file_path = f'recordings/{user_folder}/{folder_folder}/{recording_folder}/{recording_id}.mp4'

        # Procesar el video
        detected_cheats = detector.detect(unprocessed_file_path)

        # Convertir eventos a diccionarios
        detected_cheats_dict = [cheat.to_dict() for cheat in detected_cheats]

        # Devolver la respuesta JSON correctamente
        return jsonify({'processed_recording': detected_cheats_dict}), 200
    except Exception as e:
        # Devolver el error en caso de excepción
        return jsonify({'error': str(e)}), 500


@app.route('/api/delete', methods=['POST'])
def delete_recording():
    try:
        # Obtener los datos del cuerpo de la solicitud
        data = request.get_json()
        recording_url = data.get('recordingUrl')

        # Verificar si el archivo existe y eliminarlo
        if os.path.exists(recording_url):
            os.remove(recording_url)
            return jsonify({'message': 'File deleted successfully'}), 200
        else:
            return jsonify({'error': 'File not found'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True)
