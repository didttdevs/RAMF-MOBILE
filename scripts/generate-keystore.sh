#!/bin/bash

# Script para generar keystore para RAMF App
# Ejecutar desde la raíz del proyecto

echo "🔐 Generando keystore para RAMF App..."

# Crear directorio keystore si no existe
mkdir -p keystore

# Generar keystore
keytool -genkey -v \
    -keystore keystore/ramf-release-key.keystore \
    -alias ramf-release \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass ramf2024! \
    -keypass ramf2024! \
    -dname "CN=RAMF, OU=Formosa, O=Gobierno de Formosa, L=Formosa, ST=Formosa, C=AR"

echo "✅ Keystore generado exitosamente!"
echo "📁 Ubicación: keystore/ramf-release-key.keystore"
echo "🔑 Alias: ramf-release"
echo "⏰ Válido por: 10000 días"

# Crear archivo de propiedades para gradle
cat > keystore.properties << EOF
KEYSTORE_PASSWORD=ramf2024!
KEY_ALIAS=ramf-release
KEY_PASSWORD=ramf2024!
EOF

echo "📝 Archivo keystore.properties creado"
echo "⚠️  IMPORTANTE: Agregar keystore.properties al .gitignore"









