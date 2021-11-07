package controller.http;

import model.Catalogue;
import model.CatalogueQueryException;
import model.Radius;

import java.net.URI;
import java.util.List;

public interface Request {
    /**
     * Creates URI that holds full web path to respective server and that
     * is immediately ready to use.
     * According to the ASU qualifications for VizieR, it is required to decode arguments,
     * more on https://cdsarc.u-strasbg.fr/doc/asu-summary.htx
     *
     * @param catalogues list of catalogues
     * @param coordinates coordinates
     * @param radius radius
     * @param radiusType type of radius
     * @return URI object
     */
    List<URI> createDataRequest(List<Catalogue> catalogues, String coordinates, String radius, Radius radiusType);

    /**
     * Sends request and saves incoming data into "[vizier|simbad|mast]_data.txt" file. Note that
     * this file is being reused; there is no need to keep copies of these files.
     * @param request with specified catalogues (tables), radius and parameters
     */
    void sendRequest(URI request) throws CatalogueQueryException;
}
