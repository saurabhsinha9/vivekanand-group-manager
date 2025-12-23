package com.vivekanand.manager.gallery;

import com.vivekanand.manager.gallery.dto.AlbumItemDto;
import com.vivekanand.manager.gallery.dto.GalleryItemDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {
    private final GalleryService svc;
    private final AlbumRepository albums;

    public GalleryController(GalleryService s, AlbumRepository a) {
        this.svc = s;
        this.albums = a;
    }

    @GetMapping("/albums")
    public java.util.List<Album> albums() {
        return albums.findAll();
    }

    @GetMapping("/albums/{albumId}/items")
    public Page<AlbumItemDto> items(@PathVariable Long albumId, @RequestParam(defaultValue = "true") boolean onlyVisible,
                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return svc.listItems(albumId, onlyVisible, page, size);
    }
}
