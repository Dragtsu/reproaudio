package com.player.reproaudio.utils;

public enum EnumVistas {

    LOGIN("login.fxml"),
    PRODUCTO("catalogoProducto.fxml"),
    USER_HOME("main.fxml"),
    CATEGORIA("catalogoCategoria.fxml"),
    MARCA("catalogoMarca.fxml"),
    PROVEEDOR("catalogoProveedor.fxml"),

    VENTA("venta.fxml"),

    CLIENTE("catalogoCliente.fxml"),
    OFICINA("catalogoOficina.fxml"),
    VENDEDOR("catalogoVendedor.fxml"),
    MENSAJE("mensaje.fxml");

    public final String location;

    EnumVistas(String location) {
        this.location = "/com/venta/fx/views/" + location;
    }

}