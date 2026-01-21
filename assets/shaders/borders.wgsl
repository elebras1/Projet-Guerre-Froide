const MAP_WIDTH: u32 = 5616;
const MAP_HEIGHT: u32 = 2160;

struct Province {
    ownerId: u32,
    regionId: u32,
}

@group(0) @binding(0) var<storage, read> inputPixels: array<u32>;
@group(0) @binding(1) var<storage, read> dataProvinces: array<Province>;
@group(0) @binding(2) var<storage, read_write> pixelsOut: array<u32>;

fn getProvinceId(x: i32, y: i32) -> u32 {
    if (x < 0 || y < 0 || u32(x) >= MAP_WIDTH || u32(y) >= MAP_HEIGHT) {
        return 0u;
    }

    let index = u32(y) * MAP_WIDTH + u32(x);
    let pixel = inputPixels[index];

    let r = pixel & 0xFFu;
    let g = (pixel >> 8u) & 0xFFu;

    return r + (g * 256u);
}

@compute @workgroup_size(8, 8)
fn compute(@builtin(global_invocation_id) id: vec3<u32>) {
    let x = i32(id.x);
    let y = i32(id.y);

    let index = u32(y) * MAP_WIDTH + u32(x);

    let provinceCenterId = getProvinceId(x, y);

    if (provinceCenterId == 0u) {
        pixelsOut[index] = 0u;
        return;
    }

    let provinceCenterData = dataProvinces[provinceCenterId];

    let provinceRightId = getProvinceId(x + 1, y);
    let provinceLeftId = getProvinceId(x - 1, y);
    let provinceUpId = getProvinceId(x, y + 1);
    let provinceDownId = getProvinceId(x, y - 1);

    let isRightLand = provinceRightId != 0u && dataProvinces[provinceRightId].ownerId != 0u;
    let isLeftLand = provinceLeftId != 0u && dataProvinces[provinceLeftId].ownerId != 0u;
    let isUpLand = provinceUpId != 0u && dataProvinces[provinceUpId].ownerId != 0u;
    let isDownLand = provinceDownId != 0u && dataProvinces[provinceDownId].ownerId != 0u;

    var borderVal: u32 = 0u;

    if (isRightLand && isLeftLand && isUpLand && isDownLand) {
        let provinceRight = dataProvinces[provinceRightId];
        let provinceLeft = dataProvinces[provinceLeftId];
        let provinceUp = dataProvinces[provinceUpId];
        let provinceDown = dataProvinces[provinceDownId];

        if (provinceRight.ownerId != provinceCenterData.ownerId ||provinceLeft.ownerId != provinceCenterData.ownerId
            ||provinceUp.ownerId != provinceCenterData.ownerId ||provinceDown.ownerId != provinceCenterData.ownerId) {
            borderVal = 153u;
        }
        else if (provinceRight.regionId != provinceCenterData.regionId || provinceLeft.regionId != provinceCenterData.regionId
                 ||provinceUp.regionId != provinceCenterData.regionId || provinceDown.regionId != provinceCenterData.regionId) {
            borderVal = 77u;
        }
    }

    let originalPixel = inputPixels[index];
    let r = originalPixel & 0xFFu;
    let g = (originalPixel >> 8u) & 0xFFu;
    let b = (originalPixel >> 16u) & 0xFFu;

    pixelsOut[index] = r | (g << 8u) | (b << 16u) | (borderVal << 24u);
}
