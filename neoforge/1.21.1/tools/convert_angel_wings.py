#!/usr/bin/env python3
"""Convert the supplied CPM wing hierarchy, retaining original UVs and texture bytes."""
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'design/angel-animation/reference/asa.cpmproject'
ASSETS = ROOT / 'src/main/resources/assets/arthousreactions'


def convert():
    with ZipFile(SOURCE) as archive:
        config = json.loads(archive.read('config.json'))
        texture = archive.read('skin.png')
        author = json.loads(archive.read('description.json'))['desc']
    body = next(element for element in config['elements'] if element['id'] == 'body')
    original = next(child for child in body['children'] if child['name'] == 'Asas')
    counts = {'nodes': 0, 'cubes': 0, 'quads': 0}

    def vector(node, field, default):
        return [node.get(field, {}).get(axis, default) for axis in 'xyz']

    def node(element, path):
        counts['nodes'] += 1
        assert not any(element.get(flag) for flag in ['mirror', 'extrude', 'singleTex', 'glow'])
        result = {'name': element['name'], 'id': str(element['storeID']),
                  'position': vector(element, 'pos', 0), 'rotation': vector(element, 'rotation', 0),
                  'scale': vector(element, 'scale', 1), 'quads': [], 'children': []}
        if len(path) == 1:
            result['motion'] = 'wing_a' if path[0] == 0 else 'wing_b'
            result['pivot'] = [0, -23, 2]
        if element['name'] in ['B', 'B2']:
            result['motion'] = 'tip_a' if path[0] == 0 else 'tip_b'
            result['pivot'] = [0, -23, 7]
        size = vector(element, 'size', 0)
        if any(size) and element.get('show', True) and not element.get('hidden', False):
            assert element.get('texture', False) and not element.get('recolor', False)
            counts['cubes'] += 1
            offset = vector(element, 'offset', 0)
            mesh_scale = vector(element, 'rscale', 1)
            inflate = element.get('mcScale', 0)
            lo = [offset[i] - inflate for i in range(3)]
            hi = [offset[i] + size[i] * mesh_scale[i] + inflate for i in range(3)]
            x, y, z = lo
            X, Y, Z = hi
            faces = {
                'east': ([(X,y,Z),(X,y,z),(X,Y,z),(X,Y,Z)], [1,0,0]),
                'west': ([(x,y,z),(x,y,Z),(x,Y,Z),(x,Y,z)], [-1,0,0]),
                'up': ([(X,y,Z),(x,y,Z),(x,y,z),(X,y,z)], [0,-1,0]),
                'down': ([(X,Y,z),(x,Y,z),(x,Y,Z),(X,Y,Z)], [0,1,0]),
                'north': ([(X,y,z),(x,y,z),(x,Y,z),(X,Y,z)], [0,0,-1]),
                'south': ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)], [0,0,1]),
            }
            for direction, uv in element.get('faceUV', {}).items():
                positions, normal = faces[direction]
                vertices = []
                rotation = int(uv.get('rot', '0')) // 90
                # CPM starts its UV quad at the upper right and rotates in 90 degree steps.
                corners = [(uv['sx'],uv['sy']), (uv['sx'],uv['ey']),
                           (uv['ex'],uv['ey']), (uv['ex'],uv['sy'])]
                for index, position in enumerate(positions):
                    u, v = corners[(index + rotation + 3) % 4]
                    vertices.append([*position, u / config['skinSize']['x'], v / config['skinSize']['y']])
                result['quads'].append({'normal': normal, 'vertices': vertices})
                counts['quads'] += 1
        result['children'] = [node(child, (*path, index)) for index, child in enumerate(element.get('children', []))]
        return result

    model = {'source': {'file': SOURCE.name, 'sha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
                        'credit': author}, 'textureSize': [config['skinSize']['x'], config['skinSize']['y']],
             'mount': [0, 0, 4.2], 'root': node(original, ())}
    (ASSETS / 'models/effect').mkdir(parents=True, exist_ok=True)
    (ASSETS / 'models/effect/angel_wings.json').write_text(json.dumps(model, separators=(',', ':')) + '\n')
    (ASSETS / 'textures/effect/angel_wings.png').write_bytes(texture)
    print('Asas convertidas:', counts, 'textura original:', len(texture), 'bytes')


if __name__ == '__main__':
    convert()
