#!/bin/bash

# Script para build de release de RAMF App
# Ejecutar desde la raíz del proyecto

echo "🚀 Iniciando build de release para RAMF App..."

# Verificar que existe el keystore
if [ ! -f "keystore/ramf-release-key.keystore" ]; then
    echo "❌ Error: Keystore no encontrado. Ejecutar primero: ./scripts/generate-keystore.sh"
    exit 1
fi

# Verificar que existe el archivo de propiedades
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: keystore.properties no encontrado. Ejecutar primero: ./scripts/generate-keystore.sh"
    exit 1
fi

# Limpiar build anterior
echo "🧹 Limpiando build anterior..."
./gradlew clean

# Verificar que no hay errores de compilación
echo "🔍 Verificando compilación..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Error: Hay errores de compilación. Corregir antes de continuar."
    exit 1
fi

# Generar AAB para Play Store
echo "📦 Generando Android App Bundle (AAB)..."
./gradlew bundleRelease

if [ $? -eq 0 ]; then
    echo "✅ AAB generado exitosamente!"
    echo "📁 Ubicación: app/build/outputs/bundle/release/app-release.aab"
    echo "📱 Listo para subir a Play Store"
else
    echo "❌ Error al generar AAB"
    exit 1
fi

# Generar APK para testing
echo "📱 Generando APK para testing..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo "✅ APK generado exitosamente!"
    echo "📁 Ubicación: app/build/outputs/apk/release/app-release.apk"
    echo "🧪 Listo para testing"
else
    echo "❌ Error al generar APK"
    exit 1
fi

echo "🎉 Build de release completado exitosamente!"
echo "📋 Próximos pasos:"
echo "   1. Probar APK en dispositivos reales"
echo "   2. Subir AAB a Play Store"
echo "   3. Configurar listing en Play Console"









